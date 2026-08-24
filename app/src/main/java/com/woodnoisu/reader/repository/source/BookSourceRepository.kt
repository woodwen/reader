package com.woodnoisu.reader.repository.source

import androidx.lifecycle.LiveData
import com.woodnoisu.reader.model.source.BookSource
import com.woodnoisu.reader.network.HtmlService
import com.woodnoisu.reader.network.rule.RuleBookParse
import com.woodnoisu.reader.network.rule.SourceUrlRule
import com.woodnoisu.reader.persistence.BookSourceDao
import com.woodnoisu.reader.utils.LogUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

data class BookSourceImportResult(
    val total: Int,
    val enabled: Int,
    val disabled: Int,
    val unavailable: Int
)

data class BookSourceCheckResult(
    val bookSourceUrl: String,
    val displayName: String,
    val available: Boolean,
    val stage: String,
    val message: String,
    val disabled: Boolean = false
)

open class BookSourceRepository @Inject constructor(
    private val bookSourceDao: BookSourceDao,
    private val htmlService: HtmlService
) {
    private val importer = BookSourceImporter { url ->
        htmlService.getHtml(url, emptyMap())
    }

    open fun liveData(searchKey: String?): LiveData<List<BookSource>> {
        val key = searchKey?.trim().orEmpty()
        return if (key.isBlank()) {
            bookSourceDao.liveDataAll()
        } else {
            bookSourceDao.liveDataSearch("%$key%")
        }
    }

    suspend fun getSource(key: String): BookSource? {
        return bookSourceDao.getBookSource(key)
    }

    suspend fun importSource(text: String): BookSourceImportResult {
        val sources = importer.import(text)
        return insertSources(sources)
    }

    suspend fun parseSources(text: String): List<BookSource> {
        return importer.import(text)
    }

    suspend fun saveSource(source: BookSource, oldKey: String? = null): Boolean {
        if (source.bookSourceUrl.isBlank()) {
            throw IllegalArgumentException("书源地址不能为空")
        }
        if (source.bookSourceName.isBlank()) {
            throw IllegalArgumentException("书源名称不能为空")
        }
        val validation = validateSource(source)
        if (!validation.available) {
            source.enabled = false
            LogUtil.e(TAG, "保存书源不可用：${source.displayName()}，${validation.message}")
        }
        if (!oldKey.isNullOrBlank() && oldKey != source.bookSourceUrl) {
            bookSourceDao.delete(oldKey)
        }
        source.lastUpdateTime = System.currentTimeMillis()
        if (source.customOrder == 0) {
            source.customOrder = bookSourceDao.maxOrder() + 1
        }
        bookSourceDao.insert(source)
        return validation.available
    }

    suspend fun delete(source: BookSource) {
        bookSourceDao.delete(source)
    }

    suspend fun updateEnabled(source: BookSource, enabled: Boolean): Boolean {
        if (enabled) {
            val validation = validateSource(source)
            if (!validation.available) {
                LogUtil.e(TAG, "启用书源不可用：${source.displayName()}，${validation.message}")
                bookSourceDao.update(source.copy(enabled = false))
                return false
            }
        }
        bookSourceDao.update(source.copy(enabled = enabled))
        return enabled
    }

    fun toJson(source: BookSource): String {
        return BookSource.gson.toJson(source)
    }

    open suspend fun checkSourceAvailability(
        source: BookSource,
        keyword: String = DEFAULT_CHECK_KEYWORD
    ): BookSourceCheckResult {
        val current = bookSourceDao.getBookSource(source.bookSourceUrl)
            ?: return BookSourceCheckResult(
                bookSourceUrl = source.bookSourceUrl,
                displayName = source.displayName(),
                available = false,
                stage = "书源",
                message = "书源不存在"
            )
        val result = runSourceCheck(current, keyword.trim().ifBlank { DEFAULT_CHECK_KEYWORD })
        val updated = if (result.available) {
            current.copy(
                bookSourceGroup = removeFailureGroup(current.bookSourceGroup),
                bookSourceComment = removeDetectionComment(current.bookSourceComment)
            )
        } else {
            current.copy(
                enabled = false,
                bookSourceGroup = addFailureGroup(current.bookSourceGroup),
                bookSourceComment = updateDetectionComment(current.bookSourceComment, result)
            )
        }
        bookSourceDao.update(updated)
        return result.copy(disabled = !result.available && current.enabled)
    }

    private suspend fun insertSources(sources: List<BookSource>): BookSourceImportResult {
        var order = bookSourceDao.maxOrder()
        var enabledCount = 0
        var disabledCount = 0
        var unavailableCount = 0
        sources.forEach { source ->
            val old = bookSourceDao.getBookSource(source.bookSourceUrl)
            if (old != null && source.customOrder == 0) {
                source.customOrder = old.customOrder
            } else if (source.customOrder == 0) {
                source.customOrder = ++order
            }
            if (source.lastUpdateTime == 0L) {
                source.lastUpdateTime = System.currentTimeMillis()
            }
            val validation = validateSource(source)
            if (!validation.available) {
                source.enabled = false
                unavailableCount++
                LogUtil.e(TAG, "导入书源不可用：${source.displayName()}，${validation.message}")
            }
            if (source.enabled) {
                enabledCount++
            } else {
                disabledCount++
            }
        }
        if (sources.isNotEmpty()) {
            bookSourceDao.insert(*sources.toTypedArray())
        }
        return BookSourceImportResult(
            total = sources.size,
            enabled = enabledCount,
            disabled = disabledCount,
            unavailable = unavailableCount
        )
    }

    private suspend fun validateSource(source: BookSource): SourceValidation {
        if (source.bookSourceUrl.isBlank()) {
            return SourceValidation(false, "书源地址不能为空")
        }
        if (source.bookSourceName.isBlank()) {
            return SourceValidation(false, "书源名称不能为空")
        }
        val smokeRule = when {
            source.supportsExplore() -> SourceUrlRule(
                source.exploreUrl.orEmpty(),
                page = 1,
                baseUrl = source.bookSourceUrl,
                source = source
            )
            source.supportsSearch() -> SourceUrlRule(
                source.searchUrl.orEmpty(),
                key = SMOKE_SEARCH_KEY,
                page = 1,
                baseUrl = source.bookSourceUrl,
                source = source
            )
            else -> return SourceValidation(false, "书源缺少搜索或发现规则")
        }
        val body = kotlin.runCatching {
            smokeRule.load(htmlService)
        }.getOrElse {
            LogUtil.e(TAG, "书源校验请求失败：${source.displayName()}", it)
            null
        }
        if (body.isNullOrBlank()) {
            return SourceValidation(false, "书源暂时不可用")
        }
        return SourceValidation(true)
    }

    private suspend fun runSourceCheck(source: BookSource, keyword: String): BookSourceCheckResult {
        return try {
            withTimeout(CHECK_TIMEOUT_MS) {
                checkReadable(source, keyword)
            }
        } catch (e: TimeoutCancellationException) {
            checkResult(source, false, "超时", "书源检测超时")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LogUtil.e(TAG, "书源检测失败：${source.displayName()}", e)
            checkResult(source, false, "检测", e.localizedMessage ?: "书源检测失败")
        }
    }

    private suspend fun checkReadable(source: BookSource, keyword: String): BookSourceCheckResult {
        if (source.bookSourceUrl.isBlank()) {
            return checkResult(source, false, "规则", "书源地址不能为空")
        }
        if (source.bookSourceName.isBlank()) {
            return checkResult(source, false, "规则", "书源名称不能为空")
        }
        val canSearch = source.hasReadableSearchListRule()
        val canExplore = source.hasReadableExploreListRule()
        if (!canSearch && !canExplore) {
            return checkResult(source, false, "规则", "书源缺少搜索或发现规则")
        }
        source.missingReadRuleMessage()?.let {
            return checkResult(source, false, "规则", it)
        }

        val parse = RuleBookParse(htmlService, source)
        val books = arrayListOf<com.woodnoisu.reader.model.BookBean>()
        if (canSearch) {
            val response = try {
                parse.getSearchByKeyword(keyword, 1)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                return checkResult(source, false, "搜索", e.localizedMessage ?: "搜索失败")
            }
            books.addAll(response.bookBeans)
        }
        if (books.isEmpty() && canExplore) {
            val response = try {
                parse.getSearchByType(RuleBookParse.TYPE_EXPLORE, 1)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                return checkResult(source, false, "发现", e.localizedMessage ?: "发现失败")
            }
            books.addAll(response.bookBeans)
        }
        if (books.isEmpty()) {
            return checkResult(source, false, "搜索", "搜索或发现无结果")
        }

        val book = books.first()
        val detail = try {
            parse.getBookInfo(book.url)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return checkResult(source, false, "详情", e.localizedMessage ?: "详情加载失败")
        } ?: return checkResult(source, false, "详情", "详情为空")

        val chapters = try {
            parse.getChapterList(detail.url, detail.chaptersUrl, 0, 1)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return checkResult(source, false, "目录", e.localizedMessage ?: "目录加载失败")
        }
        if (chapters.isEmpty()) {
            return checkResult(source, false, "目录", "目录为空")
        }

        val content = try {
            parse.getChapterContent(chapters[0].url)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            return checkResult(source, false, "正文", e.localizedMessage ?: "正文加载失败")
        }
        if (content.isNullOrBlank()) {
            return checkResult(source, false, "正文", "正文内容为空")
        }
        return checkResult(source, true, "完成", "书源可用")
    }

    private fun BookSource.hasReadableSearchListRule(): Boolean {
        val rule = getSearchRule()
        return !searchUrl.isNullOrBlank() &&
            !rule.bookList.isNullOrBlank() &&
            !rule.name.isNullOrBlank() &&
            !rule.bookUrl.isNullOrBlank()
    }

    private fun BookSource.hasReadableExploreListRule(): Boolean {
        val rule = getExploreRule()
        return !exploreUrl.isNullOrBlank() &&
            enabledExplore &&
            !rule.bookList.isNullOrBlank() &&
            !rule.name.isNullOrBlank() &&
            !rule.bookUrl.isNullOrBlank()
    }

    private fun BookSource.missingReadRuleMessage(): String? {
        return when {
            getBookInfoRule().name.isNullOrBlank() -> "书源缺少详情名称规则"
            getTocRule().chapterList.isNullOrBlank() -> "书源缺少目录列表规则"
            getTocRule().chapterName.isNullOrBlank() -> "书源缺少章节名称规则"
            getTocRule().chapterUrl.isNullOrBlank() -> "书源缺少章节地址规则"
            getContentRule().content.isNullOrBlank() -> "书源缺少正文规则"
            else -> null
        }
    }

    private fun checkResult(
        source: BookSource,
        available: Boolean,
        stage: String,
        message: String
    ): BookSourceCheckResult {
        return BookSourceCheckResult(
            bookSourceUrl = source.bookSourceUrl,
            displayName = source.displayName(),
            available = available,
            stage = stage,
            message = message
        )
    }

    private fun addFailureGroup(group: String?): String {
        val groups = splitGroups(group).toMutableList()
        if (!groups.contains(FAILURE_GROUP)) {
            groups.add(FAILURE_GROUP)
        }
        return groups.joinToString(",")
    }

    private fun removeFailureGroup(group: String?): String? {
        return splitGroups(group)
            .filterNot { it == FAILURE_GROUP }
            .joinToString(",")
            .ifBlank { null }
    }

    private fun splitGroups(group: String?): List<String> {
        return group.orEmpty()
            .split(GROUP_SPLIT_REGEX)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun updateDetectionComment(
        comment: String?,
        result: BookSourceCheckResult
    ): String {
        val oldComment = removeDetectionComment(comment).orEmpty()
        return listOf("$DETECTION_ERROR_PREFIX${result.stage}：${result.message}", oldComment)
            .filter { it.isNotBlank() }
            .joinToString("\n")
    }

    private fun removeDetectionComment(comment: String?): String? {
        return comment.orEmpty()
            .lineSequence()
            .filterNot { it.startsWith(DETECTION_ERROR_PREFIX) }
            .joinToString("\n")
            .ifBlank { null }
    }

    private data class SourceValidation(
        val available: Boolean,
        val message: String = ""
    )

    companion object {
        const val DEFAULT_CHECK_KEYWORD = "我的"
        const val DETECTION_ERROR_PREFIX = "检测失败："
        const val FAILURE_GROUP = "失效"
        private const val TAG = "BookSourceRepository"
        private const val SMOKE_SEARCH_KEY = "测试"
        private const val CHECK_TIMEOUT_MS = 30_000L
        private val GROUP_SPLIT_REGEX = Regex("[,;，；]")
    }
}

package com.woodnoisu.reader.repository.source

import androidx.lifecycle.LiveData
import com.woodnoisu.reader.model.source.BookSource
import com.woodnoisu.reader.network.HtmlService
import com.woodnoisu.reader.network.rule.SourceUrlRule
import com.woodnoisu.reader.persistence.BookSourceDao
import com.woodnoisu.reader.utils.LogUtil
import javax.inject.Inject

data class BookSourceImportResult(
    val total: Int,
    val enabled: Int,
    val disabled: Int,
    val unavailable: Int
)

class BookSourceRepository @Inject constructor(
    private val bookSourceDao: BookSourceDao,
    private val htmlService: HtmlService
) {
    private val importer = BookSourceImporter { url ->
        htmlService.getHtml(url, emptyMap())
    }

    fun liveData(searchKey: String?): LiveData<List<BookSource>> {
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

    private data class SourceValidation(
        val available: Boolean,
        val message: String = ""
    )

    companion object {
        private const val TAG = "BookSourceRepository"
        private const val SMOKE_SEARCH_KEY = "测试"
    }
}

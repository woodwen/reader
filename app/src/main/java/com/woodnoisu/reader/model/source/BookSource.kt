package com.woodnoisu.reader.model.source

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.woodnoisu.reader.constant.Constant
import com.woodnoisu.reader.model.source.rule.BookInfoRule
import com.woodnoisu.reader.model.source.rule.ContentRule
import com.woodnoisu.reader.model.source.rule.ExploreRule
import com.woodnoisu.reader.model.source.rule.SearchRule
import com.woodnoisu.reader.model.source.rule.TocRule
import kotlinx.parcelize.Parcelize

@Parcelize
@TypeConverters(BookSource.Converters::class)
@Entity(
    tableName = "book_sources",
    indices = [Index(value = ["bookSourceUrl"], unique = false)]
)
data class BookSource(
    var bookSourceName: String = "",
    var bookSourceGroup: String? = null,
    @PrimaryKey
    var bookSourceUrl: String = "",
    var bookSourceType: Int = TYPE_TEXT,
    var bookUrlPattern: String? = null,
    var customOrder: Int = 0,
    var enabled: Boolean = true,
    var enabledExplore: Boolean = true,
    var header: String? = null,
    var loginUrl: String? = null,
    var bookSourceComment: String? = null,
    var lastUpdateTime: Long = 0,
    var weight: Int = 0,
    var exploreUrl: String? = null,
    var ruleExplore: ExploreRule? = null,
    var searchUrl: String? = null,
    var ruleSearch: SearchRule? = null,
    var ruleBookInfo: BookInfoRule? = null,
    var ruleToc: TocRule? = null,
    var ruleContent: ContentRule? = null
) : Parcelable {

    fun displayName(): String = bookSourceName.ifBlank { bookSourceUrl }

    fun supportsSearch(): Boolean =
        !searchUrl.isNullOrBlank() && !getSearchRule().bookList.isNullOrBlank()

    fun supportsReadableSearch(): Boolean =
        supportsSearch() &&
            isSupportedReadRule(getSearchRule().bookList) &&
            isSupportedReadRule(getSearchRule().name) &&
            isSupportedReadRule(getSearchRule().author) &&
            isSupportedReadRule(getSearchRule().intro) &&
            isSupportedReadRule(getSearchRule().kind) &&
            isSupportedReadRule(getSearchRule().lastChapter) &&
            isSupportedReadRule(getSearchRule().updateTime) &&
            isSupportedReadRule(getSearchRule().bookUrl) &&
            isSupportedReadRule(getSearchRule().coverUrl) &&
            !getSearchRule().name.isNullOrBlank() &&
            !getSearchRule().bookUrl.isNullOrBlank() &&
            !getBookInfoRule().name.isNullOrBlank() &&
            !getTocRule().chapterList.isNullOrBlank() &&
            !getTocRule().chapterName.isNullOrBlank() &&
            !getTocRule().chapterUrl.isNullOrBlank() &&
            !getContentRule().content.isNullOrBlank() &&
            isSupportedReadRule(getBookInfoRule().init) &&
            isSupportedReadRule(getBookInfoRule().name) &&
            isSupportedReadRule(getBookInfoRule().tocUrl) &&
            isSupportedReadRule(getTocRule().chapterList) &&
            isSupportedReadRule(getTocRule().chapterName) &&
            isSupportedReadRule(getTocRule().chapterUrl) &&
            isSupportedReadRule(getTocRule().nextTocUrl) &&
            isSupportedReadRule(getContentRule().content)

    fun supportsExplore(): Boolean =
        !exploreUrl.isNullOrBlank() && !getExploreRule().bookList.isNullOrBlank()

    fun getExploreRule(): ExploreRule = ruleExplore ?: ExploreRule()

    fun getSearchRule(): SearchRule = ruleSearch ?: SearchRule()

    fun getBookInfoRule(): BookInfoRule = ruleBookInfo ?: BookInfoRule()

    fun getTocRule(): TocRule = ruleToc ?: TocRule()

    fun getContentRule(): ContentRule = ruleContent ?: ContentRule()

    fun getHeaderMap(): Map<String, String> {
        val headers = linkedMapOf("User-Agent" to Constant.UserAgent)
        val headerText = header?.trim().orEmpty()
        if (headerText.isNotEmpty()) {
            kotlin.runCatching {
                val type = object : TypeToken<Map<String, String>>() {}.type
                val custom: Map<String, String>? = gson.fromJson(headerText, type)
                custom?.let { headers.putAll(it) }
            }
        }
        return headers
    }

    private fun isSupportedReadRule(rule: String?): Boolean {
        val text = rule?.lowercase().orEmpty()
        if (text.isBlank()) return true
        return !unsupportedReadTokens.any { text.contains(it) }
    }

    class Converters {
        @TypeConverter
        fun exploreRuleToString(rule: ExploreRule?): String? = gson.toJson(rule)

        @TypeConverter
        fun stringToExploreRule(json: String?): ExploreRule? = fromJson(json)

        @TypeConverter
        fun searchRuleToString(rule: SearchRule?): String? = gson.toJson(rule)

        @TypeConverter
        fun stringToSearchRule(json: String?): SearchRule? = fromJson(json)

        @TypeConverter
        fun bookInfoRuleToString(rule: BookInfoRule?): String? = gson.toJson(rule)

        @TypeConverter
        fun stringToBookInfoRule(json: String?): BookInfoRule? = fromJson(json)

        @TypeConverter
        fun tocRuleToString(rule: TocRule?): String? = gson.toJson(rule)

        @TypeConverter
        fun stringToTocRule(json: String?): TocRule? = fromJson(json)

        @TypeConverter
        fun contentRuleToString(rule: ContentRule?): String? = gson.toJson(rule)

        @TypeConverter
        fun stringToContentRule(json: String?): ContentRule? = fromJson(json)

        private inline fun <reified T> fromJson(json: String?): T? {
            if (json.isNullOrBlank() || json == "null") return null
            return kotlin.runCatching { gson.fromJson(json, T::class.java) }.getOrNull()
        }
    }

    companion object {
        const val TYPE_TEXT = 0
        const val TYPE_AUDIO = 1

        private val unsupportedReadTokens = listOf(
            "@js",
            "<js",
            "java.ajax",
            "webview",
            "textnodes"
        )

        val gson: Gson = GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()
    }
}

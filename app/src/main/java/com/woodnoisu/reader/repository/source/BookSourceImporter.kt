package com.woodnoisu.reader.repository.source

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.woodnoisu.reader.model.source.BookSource
import com.woodnoisu.reader.model.source.rule.BookInfoRule
import com.woodnoisu.reader.model.source.rule.ContentRule
import com.woodnoisu.reader.model.source.rule.ExploreRule
import com.woodnoisu.reader.model.source.rule.SearchRule
import com.woodnoisu.reader.model.source.rule.TocRule
import com.woodnoisu.reader.network.rule.NetworkRuleUtils
import java.net.URLDecoder
import java.util.regex.Pattern

class BookSourceImporter(
    private val loadUrl: suspend (String) -> String?
) {
    suspend fun import(text: String): List<BookSource> {
        val sources = linkedMapOf<String, BookSource>()
        importInternal(text.trim(), sources)
        return sources.values.toList()
    }

    private suspend fun importInternal(text: String, out: LinkedHashMap<String, BookSource>) {
        if (text.isBlank()) return
        val normalized = normalizeImportText(text)
        when {
            NetworkRuleUtils.isAbsUrl(normalized) -> {
                val body = loadUrl(normalized) ?: throw IllegalArgumentException("在线书源读取失败")
                importInternal(body, out)
            }
            normalized.isJsonObject() -> {
                val jsonObject = JsonParser().parse(normalized).asJsonObject
                val sourceUrls = jsonObject.getAsJsonArray("sourceUrls")
                if (sourceUrls != null) {
                    importUrls(sourceUrls, out)
                } else {
                    jsonObject.toBookSource()?.let { out[it.bookSourceUrl] = it }
                }
            }
            normalized.isJsonArray() -> {
                JsonParser().parse(normalized).asJsonArray.forEach { item ->
                    item.asJsonObject.toBookSource()?.let { out[it.bookSourceUrl] = it }
                }
            }
            else -> throw IllegalArgumentException("书源格式不正确")
        }
    }

    private suspend fun importUrls(sourceUrls: JsonArray, out: LinkedHashMap<String, BookSource>) {
        sourceUrls.forEach { item ->
            val url = item.asString
            if (url.isNotBlank()) {
                importInternal(url, out)
            }
        }
    }

    private fun normalizeImportText(text: String): String {
        if (!text.startsWith("yuedu://booksource", true)) return text
        val src = text.substringAfter("src=", "")
            .substringBefore("&")
        return URLDecoder.decode(src, "UTF-8")
    }

    private fun JsonObject.toBookSource(): BookSource? {
        val source = if (has("ruleToc") || has("searchUrl") || has("ruleSearch")) {
            toNewBookSource()
        } else {
            toOldBookSource()
        }
        if (source.bookSourceUrl.isBlank() || source.bookSourceName.isBlank()) return null
        if (source.lastUpdateTime == 0L) {
            source.lastUpdateTime = System.currentTimeMillis()
        }
        return source
    }

    private fun JsonObject.toNewBookSource(): BookSource {
        return BookSource(
            bookSourceName = readString("bookSourceName").orEmpty(),
            bookSourceGroup = readString("bookSourceGroup"),
            bookSourceUrl = readString("bookSourceUrl").orEmpty(),
            bookSourceType = readSourceType("bookSourceType"),
            bookUrlPattern = readString("bookUrlPattern"),
            customOrder = readInt("customOrder") ?: 0,
            enabled = readBool("enabled") ?: true,
            enabledExplore = readBool("enabledExplore") ?: true,
            header = readString("header"),
            loginUrl = readString("loginUrl"),
            bookSourceComment = readString("bookSourceComment"),
            lastUpdateTime = readLong("lastUpdateTime") ?: 0,
            weight = readInt("weight") ?: 0,
            exploreUrl = readString("exploreUrl"),
            ruleExplore = readRule("ruleExplore", ExploreRule::class.java),
            searchUrl = readString("searchUrl"),
            ruleSearch = readRule("ruleSearch", SearchRule::class.java),
            ruleBookInfo = readRule("ruleBookInfo", BookInfoRule::class.java),
            ruleToc = readRule("ruleToc", TocRule::class.java),
            ruleContent = readRule("ruleContent", ContentRule::class.java)
        )
    }

    private fun JsonObject.toOldBookSource(): BookSource {
        val exploreUrl = readString("ruleFindUrl").toNewUrls()
        return BookSource(
            bookSourceName = readString("bookSourceName").orEmpty(),
            bookSourceGroup = readString("bookSourceGroup"),
            bookSourceUrl = readString("bookSourceUrl").orEmpty(),
            bookSourceType = readSourceType("bookSourceType"),
            bookUrlPattern = readString("ruleBookUrlPattern"),
            customOrder = readInt("serialNumber") ?: 0,
            enabled = readBool("enable") ?: true,
            enabledExplore = !exploreUrl.isNullOrBlank(),
            header = readString("httpUserAgent").uaToHeader(),
            loginUrl = readString("loginUrl"),
            bookSourceComment = readString("bookSourceComment"),
            exploreUrl = exploreUrl,
            searchUrl = readString("ruleSearchUrl").toNewUrl(),
            ruleSearch = SearchRule(
                bookList = readString("ruleSearchList").toNewRule(),
                name = readString("ruleSearchName").toNewRule(),
                author = readString("ruleSearchAuthor").toNewRule(),
                intro = readString("ruleSearchIntroduce").toNewRule(),
                kind = readString("ruleSearchKind").toNewRule(),
                bookUrl = readString("ruleSearchNoteUrl").toNewRule(),
                coverUrl = readString("ruleSearchCoverUrl").toNewRule(),
                lastChapter = readString("ruleSearchLastChapter").toNewRule()
            ),
            ruleExplore = ExploreRule(
                bookList = readString("ruleFindList").toNewRule(),
                name = readString("ruleFindName").toNewRule(),
                author = readString("ruleFindAuthor").toNewRule(),
                intro = readString("ruleFindIntroduce").toNewRule(),
                kind = readString("ruleFindKind").toNewRule(),
                bookUrl = readString("ruleFindNoteUrl").toNewRule(),
                coverUrl = readString("ruleFindCoverUrl").toNewRule(),
                lastChapter = readString("ruleFindLastChapter").toNewRule()
            ),
            ruleBookInfo = BookInfoRule(
                init = readString("ruleBookInfoInit").toNewRule(),
                name = readString("ruleBookName").toNewRule(),
                author = readString("ruleBookAuthor").toNewRule(),
                intro = readString("ruleIntroduce").toNewRule(),
                kind = readString("ruleBookKind").toNewRule(),
                coverUrl = readString("ruleCoverUrl").toNewRule(),
                lastChapter = readString("ruleBookLastChapter").toNewRule(),
                tocUrl = readString("ruleChapterUrl").toNewRule()
            ),
            ruleToc = TocRule(
                chapterList = readString("ruleChapterList").toNewRule(),
                chapterName = readString("ruleChapterName").toNewRule(),
                chapterUrl = readString("ruleContentUrl").toNewRule(),
                nextTocUrl = readString("ruleChapterUrlNext").toNewRule()
            ),
            ruleContent = ContentRule(
                content = readString("ruleBookContent").toNewContentRule(),
                replaceRegex = readString("ruleBookContentReplace").toNewRule(),
                nextContentUrl = readString("ruleContentUrlNext").toNewRule()
            )
        )
    }

    private fun JsonObject.readString(name: String): String? {
        val element = get(name) ?: return null
        if (element.isJsonNull) return null
        return kotlin.runCatching { element.asString }.getOrNull()
    }

    private fun JsonObject.readInt(name: String): Int? {
        val element = get(name) ?: return null
        if (element.isJsonNull) return null
        return kotlin.runCatching { element.asInt }.getOrNull()
    }

    private fun JsonObject.readLong(name: String): Long? {
        val element = get(name) ?: return null
        if (element.isJsonNull) return null
        return kotlin.runCatching { element.asLong }.getOrNull()
    }

    private fun JsonObject.readBool(name: String): Boolean? {
        val element = get(name) ?: return null
        if (element.isJsonNull) return null
        return kotlin.runCatching { element.asBoolean }.getOrNull()
    }

    private fun JsonObject.readSourceType(name: String): Int {
        val value = readString(name)
        if (value.equals("AUDIO", true)) return BookSource.TYPE_AUDIO
        return readInt(name) ?: BookSource.TYPE_TEXT
    }

    private fun <T> JsonObject.readRule(name: String, clazz: Class<T>): T? {
        val element: JsonElement = get(name) ?: return null
        if (element.isJsonNull) return null
        val json = if (element.isJsonPrimitive && element.asJsonPrimitive.isString) {
            element.asString
        } else {
            element.toString()
        }
        return kotlin.runCatching { BookSource.gson.fromJson(json, clazz) }.getOrNull()
    }

    private fun String?.isJsonObject(): Boolean {
        val str = this?.trim().orEmpty()
        return str.startsWith("{") && str.endsWith("}")
    }

    private fun String?.isJsonArray(): Boolean {
        val str = this?.trim().orEmpty()
        return str.startsWith("[") && str.endsWith("]")
    }

    private fun String?.toNewRule(): String? {
        if (this.isNullOrBlank()) return null
        var newRule = this
        var reverse = false
        var allInOne = false
        if (newRule.startsWith("-")) {
            reverse = true
            newRule = newRule.substring(1)
        }
        if (newRule.startsWith("+")) {
            allInOne = true
            newRule = newRule.substring(1)
        }
        if (!newRule.startsWith("@CSS:", true) &&
            !newRule.startsWith("@XPath:", true) &&
            !newRule.startsWith("//") &&
            !newRule.startsWith("##") &&
            !newRule.startsWith(":") &&
            !newRule.contains("@js:", true) &&
            !newRule.contains("<js>", true)
        ) {
            if (newRule.contains("#") && !newRule.contains("##")) {
                newRule = newRule.replace("#", "##")
            }
            if (newRule.contains("|") && !newRule.contains("||")) {
                newRule = newRule.replace("|", "||")
            }
            if (newRule.contains("&") &&
                !newRule.contains("&&") &&
                !newRule.contains("http") &&
                !newRule.startsWith("/")
            ) {
                newRule = newRule.replace("&", "&&")
            }
        }
        if (allInOne) newRule = "+$newRule"
        if (reverse) newRule = "-$newRule"
        return newRule
    }

    private fun String?.toNewContentRule(): String? {
        val rule = toNewRule() ?: return null
        return if (rule.startsWith("$") && !rule.startsWith("$.")) rule.substring(1) else rule
    }

    private fun String?.toNewUrls(): String? {
        if (this.isNullOrBlank()) return null
        if (!contains("\n") && !contains("&&")) return toNewUrl()
        return split(Regex("(&&|\\r?\\n)+"))
            .mapNotNull { it.toNewUrl()?.replace(Regex("\\n\\s*"), "") }
            .joinToString("\n")
    }

    private fun String?.toNewUrl(): String? {
        if (this.isNullOrBlank()) return null
        var url = this
        if (url.startsWith("<js>", true)) {
            return url.replace("=searchKey", "={{key}}")
                .replace("=searchPage", "={{page}}")
        }
        val map = linkedMapOf<String, String>()
        val headerMatcher = headerPattern.matcher(url)
        if (headerMatcher.find()) {
            val header = headerMatcher.group()
            url = url.replace(header, "")
            map["headers"] = header.substring(8)
        }
        val urlList = url.split("|")
        url = urlList[0]
        if (urlList.size > 1) {
            map["charset"] = urlList[1].split("=").getOrNull(1).orEmpty()
        }
        url = url.replace("{", "<")
            .replace("}", ">")
            .replace("searchKey", "{{key}}")
            .replace(Regex("<searchPage([-+]1)>"), "{{page$1}}")
            .replace(Regex("searchPage([-+]1)"), "{{page$1}}")
            .replace("searchPage", "{{page}}")
        val postSplit = url.split("@", limit = 2)
        url = postSplit[0]
        if (postSplit.size > 1) {
            map["method"] = "POST"
            map["body"] = postSplit[1]
        }
        if (map.isNotEmpty()) {
            url += "," + BookSource.gson.toJson(map)
        }
        return url
    }

    private fun String?.uaToHeader(): String? {
        if (this.isNullOrBlank()) return null
        return BookSource.gson.toJson(mapOf("User-Agent" to this))
    }

    companion object {
        private val headerPattern = Pattern.compile("@Header:\\{.+?\\}", Pattern.CASE_INSENSITIVE)
    }
}

package com.woodnoisu.reader.network.rule

import java.net.URL
import java.net.URLEncoder
import java.util.regex.Pattern

object NetworkRuleUtils {
    private val pagePattern = Pattern.compile("<(.*?)>")

    fun isAbsUrl(value: String?): Boolean =
        value?.startsWith("http://", true) == true || value?.startsWith("https://", true) == true

    fun absoluteUrl(baseUrl: String?, relativePath: String?): String {
        if (relativePath.isNullOrBlank()) return baseUrl.orEmpty()
        if (isAbsUrl(relativePath)) return relativePath
        if (baseUrl.isNullOrBlank()) return relativePath
        return kotlin.runCatching {
            URL(URL(baseUrl.substringBefore(",")), relativePath).toString()
        }.getOrDefault(relativePath)
    }

    fun baseUrl(url: String?): String? {
        if (url.isNullOrBlank() || !url.startsWith("http", true)) return null
        val index = url.indexOf("/", 9)
        return if (index == -1) url else url.substring(0, index)
    }

    fun replaceUrlParams(rule: String, key: String? = null, page: Int? = null): String {
        var result = rule
        page?.let {
            val matcher = pagePattern.matcher(result)
            while (matcher.find()) {
                val pages = matcher.group(1).orEmpty().split(",")
                val replacement = if (it <= pages.size) {
                    pages[it - 1].trim()
                } else {
                    pages.last().trim()
                }
                result = result.replace(matcher.group(), replacement)
            }
        }
        key?.let {
            val encodedKey = URLEncoder.encode(it, "UTF-8")
            result = result.replace("{{key}}", encodedKey)
                .replace("{{searchKey}}", encodedKey)
                .replace("searchKey", encodedKey)
        }
        page?.let {
            result = result.replace("{{page}}", it.toString())
                .replace("{{searchPage}}", it.toString())
                .replace("searchPage", it.toString())
            result = result.replace(Regex("\\{\\{page([+-]\\d+)\\}\\}")) { match ->
                (it + match.groupValues[1].toInt()).toString()
            }
        }
        return result
    }

    fun splitNotBlank(value: String, delimiter: String): List<String> =
        value.split(delimiter).map { it.trim() }.filter { it.isNotBlank() }

    fun joinNonEmpty(items: List<String>): String = items.filter { it.isNotBlank() }.joinToString("\n")
}

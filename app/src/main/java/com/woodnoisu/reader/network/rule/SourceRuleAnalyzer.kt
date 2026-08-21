package com.woodnoisu.reader.network.rule

import com.jayway.jsonpath.JsonPath
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.seimicrawler.xpath.JXDocument
import org.seimicrawler.xpath.JXNode
import java.net.URL
import java.util.regex.Pattern

class SourceRuleAnalyzer(
    content: Any,
    private val baseUrl: String = "",
    redirectUrl: String = baseUrl,
    private val ruleData: RuleData = RuleData()
) {
    private var content: Any = content
    private var redirectUrl: URL? = kotlin.runCatching {
        URL(redirectUrl.substringBefore(","))
    }.getOrNull()

    fun setContent(value: Any): SourceRuleAnalyzer {
        content = value
        return this
    }

    fun getElement(rule: String?): Any? = getElements(rule).firstOrNull()

    fun getElements(rule: String?): List<Any> {
        if (rule.isNullOrBlank()) return emptyList()
        val sourceRule = ParsedRule(rule)
        val raw = when (sourceRule.mode) {
            Mode.JSON -> jsonList(content, sourceRule.rule)
            Mode.XPATH -> xpathElements(content, sourceRule.rule)
            Mode.REGEX -> regexElements(content.toString(), sourceRule.rule)
            Mode.DEFAULT -> cssElements(content, sourceRule.rule)
        }
        return if (sourceRule.hasReplace) {
            raw.map { sourceRule.applyReplace(it.toString()) }
        } else {
            raw
        }
    }

    fun getString(rule: String?, isUrl: Boolean = false, value: String? = null): String {
        if (rule.isNullOrBlank()) return ""
        val sourceRule = ParsedRule(rule)
        val input = value ?: content
        val raw = when (sourceRule.mode) {
            Mode.JSON -> NetworkRuleUtils.joinNonEmpty(jsonStrings(input, sourceRule.rule))
            Mode.XPATH -> NetworkRuleUtils.joinNonEmpty(xpathStrings(input, sourceRule.rule))
            Mode.REGEX -> NetworkRuleUtils.joinNonEmpty(regexStrings(input.toString(), sourceRule.rule))
            Mode.DEFAULT -> NetworkRuleUtils.joinNonEmpty(cssStrings(input, sourceRule.rule))
        }
        val result = sourceRule.applyReplace(raw)
        return if (isUrl) NetworkRuleUtils.absoluteUrl(redirectUrl?.toString() ?: baseUrl, result) else result
    }

    fun put(key: String, value: String): String {
        ruleData.putVariable(key, value)
        return value
    }

    fun get(key: String): String = ruleData.variableMap[key].orEmpty()

    private fun cssElements(input: Any, rule: String): List<Any> {
        val rules = splitAlternative(rule)
        for (oneRule in rules) {
            val elements = cssElementsSingle(input, oneRule)
            if (elements.isNotEmpty()) return elements
        }
        return emptyList()
    }

    private fun cssElementsSingle(input: Any, rule: String): List<Element> {
        var elements = inputElements(input)
        val segments = rule.removePrefix("@CSS:").split("@").filter { it.isNotBlank() }
        if (segments.isEmpty()) return elements
        for ((index, segment) in segments.withIndex()) {
            if (index == segments.lastIndex && segments.size > 1 && isValueSegment(segment)) break
            val next = Elements()
            elements.forEach {
                next.addAll(selectByDefaultRule(it, segment))
            }
            elements = next
        }
        return elements
    }

    private fun cssStrings(input: Any, rule: String): List<String> {
        val rules = splitAlternative(rule)
        for (oneRule in rules) {
            val strings = cssStringsSingle(input, oneRule)
            if (strings.isNotEmpty()) return strings
        }
        return emptyList()
    }

    private fun cssStringsSingle(input: Any, rule: String): List<String> {
        val normalized = rule.removePrefix("@CSS:")
        val segments = normalized.split("@").filter { it.isNotBlank() }
        val valueSegment = segments.lastOrNull()?.takeIf {
            if (segments.size > 1) isValueSegment(it) else isDirectValueSegment(it)
        }
        val elementRule = if (valueSegment == null) normalized else segments.dropLast(1).joinToString("@")
        val elements = if (elementRule.isBlank()) inputElements(input) else cssElementsSingle(input, elementRule)
        return elements.mapNotNull {
            valueFromElement(it, valueSegment)
        }.filter { it.isNotBlank() }
    }

    private fun selectByDefaultRule(element: Element, rule: String): Elements {
        val result = Elements()
        val excludeSplit = rule.split("!", limit = 2)
        val selector = excludeSplit[0].trim()
        when {
            selector == "children" -> result.addAll(element.children())
            selector.startsWith("class.") -> {
                val parts = selector.split(".")
                result.addAll(element.getElementsByClass(parts.getOrElse(1) { "" }))
                applyIndex(result, parts.getOrNull(2))
            }
            selector.startsWith("id.") -> {
                val parts = selector.split(".")
                result.addAll(element.getElementsByAttributeValue("id", parts.getOrElse(1) { "" }))
                applyIndex(result, parts.getOrNull(2))
            }
            selector.startsWith("tag.") -> {
                val parts = selector.split(".")
                result.addAll(element.getElementsByTag(parts.getOrElse(1) { "" }))
                applyIndex(result, parts.getOrNull(2))
            }
            selector.startsWith("text.") -> result.addAll(element.getElementsContainingOwnText(selector.substringAfter("text.")))
            selector.isNotBlank() -> result.addAll(element.select(selector))
        }
        excludeSplit.getOrNull(1)?.split(":")?.mapNotNull { it.toIntOrNull() }?.sortedDescending()?.forEach {
            val index = if (it < 0) result.size + it else it
            if (index in 0 until result.size) result.removeAt(index)
        }
        return result
    }

    private fun applyIndex(elements: Elements, indexText: String?) {
        val index = indexText?.toIntOrNull() ?: return
        if (elements.isEmpty()) return
        val realIndex = if (index < 0) elements.size + index else index
        val item = elements.getOrNull(realIndex)
        elements.clear()
        item?.let { elements.add(it) }
    }

    private fun valueFromElement(element: Element, valueSegment: String?): String {
        return when {
            valueSegment == null || valueSegment == "text" -> element.text()
            valueSegment == "ownText" -> element.ownText()
            valueSegment == "html" -> element.html()
            valueSegment == "all" -> element.outerHtml()
            valueSegment == "href" -> element.attr("href")
            valueSegment == "src" -> element.attr("src")
            else -> element.attr(valueSegment)
        }
    }

    private fun isValueSegment(segment: String): Boolean {
        return segment in setOf("text", "ownText", "html", "all", "href", "src") ||
            (!segment.contains(".") && !segment.startsWith("#") && !segment.startsWith("["))
    }

    private fun isDirectValueSegment(segment: String): Boolean {
        return segment in setOf("text", "ownText", "html", "all", "href", "src")
    }

    private fun inputElements(input: Any): Elements {
        return when (input) {
            is Element -> Elements(input)
            is Elements -> input
            is JXNode -> if (input.isElement) Elements(input.asElement()) else Elements(Jsoup.parse(input.toString()))
            else -> Elements(Jsoup.parse(input.toString()))
        }
    }

    private fun xpathElements(input: Any, rule: String): List<Any> {
        val doc = when (input) {
            is JXNode -> return input.sel(rule).map { it as Any }
            is Element -> JXDocument.create(Elements(input))
            is Elements -> JXDocument.create(input)
            else -> JXDocument.create(input.toString())
        }
        return doc.selN(rule).map { it as Any }
    }

    private fun xpathStrings(input: Any, rule: String): List<String> =
        xpathElements(input, rule).map { it.toString() }.filter { it.isNotBlank() }

    private fun jsonList(input: Any, rule: String): List<Any> {
        return kotlin.runCatching {
            val value = JsonPath.parse(input).read<Any>(rule)
            when (value) {
                is List<*> -> value.filterNotNull()
                null -> emptyList()
                else -> listOf(value)
            }
        }.getOrDefault(emptyList())
    }

    private fun jsonStrings(input: Any, rule: String): List<String> =
        jsonList(input, rule).map { it.toString() }.filter { it.isNotBlank() }

    private fun regexElements(input: String, rule: String): List<Any> =
        regexStrings(input, rule).map { it as Any }

    private fun regexStrings(input: String, rule: String): List<String> {
        val regexes = rule.split("&&").filter { it.isNotBlank() }
        if (regexes.isEmpty()) return emptyList()
        var current = listOf(input)
        regexes.forEach { regex ->
            val next = arrayListOf<String>()
            val pattern = Pattern.compile(regex)
            current.forEach { item ->
                val matcher = pattern.matcher(item)
                while (matcher.find()) {
                    next.add(if (matcher.groupCount() >= 1) matcher.group(1) else matcher.group())
                }
            }
            current = next
        }
        return current
    }

    private fun splitAlternative(rule: String): List<String> {
        val delimiter = when {
            rule.contains("||") -> "||"
            else -> null
        }
        return delimiter?.let { NetworkRuleUtils.splitNotBlank(rule, it) } ?: listOf(rule)
    }

    private class ParsedRule(ruleText: String) {
        val mode: Mode
        val rule: String
        val hasReplace: Boolean
        private val replaceRegex: String?
        private val replacement: String

        init {
            var text = ruleText.trim()
            mode = when {
                text.startsWith("@XPath:", true) -> {
                    text = text.substring(7)
                    Mode.XPATH
                }
                text.startsWith("//") -> Mode.XPATH
                text.startsWith("@Json:", true) -> {
                    text = text.substring(6)
                    Mode.JSON
                }
                text.startsWith("$.") -> Mode.JSON
                text.startsWith(":") -> {
                    text = text.substring(1)
                    Mode.REGEX
                }
                text.startsWith("@CSS:", true) -> Mode.DEFAULT
                else -> Mode.DEFAULT
            }
            val parts = text.split("##")
            rule = parts[0].trim()
            replaceRegex = parts.getOrNull(1)
            hasReplace = !replaceRegex.isNullOrEmpty()
            replacement = parts.getOrNull(2).orEmpty()
        }

        fun applyReplace(value: String): String {
            val regex = replaceRegex
            return if (regex.isNullOrEmpty()) value else value.replace(Regex(regex), replacement)
        }
    }

    private enum class Mode {
        DEFAULT, XPATH, JSON, REGEX
    }
}

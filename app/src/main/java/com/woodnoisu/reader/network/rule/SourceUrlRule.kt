package com.woodnoisu.reader.network.rule

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.woodnoisu.reader.model.source.BookSource
import com.woodnoisu.reader.network.HtmlService

class SourceUrlRule(
    ruleUrl: String,
    key: String? = null,
    page: Int? = null,
    baseUrl: String = "",
    source: BookSource? = null
) {
    val url: String
    val headers: MutableMap<String, String> = linkedMapOf()
    val body: MutableMap<String, String> = linkedMapOf()
    val method: String

    init {
        source?.getHeaderMap()?.let { headers.putAll(it) }
        val optionSplit = ruleUrl.split(Regex(",\\s*(?=\\{)"), 2)
        val rawUrl = NetworkRuleUtils.replaceUrlParams(optionSplit[0].trim(), key, page)
        url = NetworkRuleUtils.absoluteUrl(baseUrl, rawUrl)
        var parsedMethod = "GET"
        if (optionSplit.size > 1) {
            kotlin.runCatching {
                val option = JsonParser().parse(optionSplit[1]).asJsonObject
                option.get("method")?.asString?.let {
                    parsedMethod = it.uppercase()
                }
                option.get("headers")?.let { header ->
                    readHeaders(header)
                }
                option.get("body")?.let { bodyElement ->
                    val bodyText = if (bodyElement.isJsonPrimitive) {
                        bodyElement.asString
                    } else {
                        bodyElement.toString()
                    }
                    readBody(NetworkRuleUtils.replaceUrlParams(bodyText, key, page))
                }
            }
        }
        method = parsedMethod
    }

    suspend fun load(htmlService: HtmlService): String? {
        return if (method == "POST") {
            htmlService.postHtml(url, headers, body)
        } else {
            htmlService.getHtml(url, headers)
        }
    }

    private fun readHeaders(value: com.google.gson.JsonElement) {
        val json = when {
            value.isJsonObject -> value.asJsonObject
            value.isJsonPrimitive -> kotlin.runCatching {
                JsonParser().parse(value.asString).asJsonObject
            }.getOrNull()
            else -> null
        } ?: return
        json.entrySet().forEach {
            if (!it.value.isJsonNull) {
                headers[it.key] = it.value.asString
            }
        }
    }

    private fun readBody(value: String) {
        if (value.trim().startsWith("{")) return
        value.split("&").forEach { pair ->
            val parts = pair.split("=", limit = 2)
            if (parts.isNotEmpty() && parts[0].isNotBlank()) {
                body[parts[0]] = parts.getOrNull(1).orEmpty()
            }
        }
    }
}

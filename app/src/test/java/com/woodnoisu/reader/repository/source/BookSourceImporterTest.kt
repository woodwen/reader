package com.woodnoisu.reader.repository.source

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.net.URLEncoder

class BookSourceImporterTest {

    @Test
    fun importNewSourceObject() = runBlocking {
        val importer = BookSourceImporter { null }

        val sources = importer.import(
            """
            {
              "bookSourceName": "新源",
              "bookSourceUrl": "https://source.example",
              "enabled": false,
              "searchUrl": "https://source.example/search?q={{key}}",
              "ruleSearch": {
                "bookList": ".result",
                "name": ".name@text",
                "bookUrl": ".name@href"
              }
            }
            """.trimIndent()
        )

        assertEquals(1, sources.size)
        assertEquals("新源", sources[0].bookSourceName)
        assertEquals("https://source.example", sources[0].bookSourceUrl)
        assertFalse(sources[0].enabled)
        assertEquals(".result", sources[0].ruleSearch?.bookList)
    }

    @Test
    fun importOldSourceObject() = runBlocking {
        val importer = BookSourceImporter { null }

        val sources = importer.import(
            """
            {
              "bookSourceName": "旧源",
              "bookSourceUrl": "https://old.example",
              "enable": true,
              "ruleSearchUrl": "https://old.example/search?keyword=searchKey&page=searchPage",
              "ruleSearchList": "div.result",
              "ruleSearchName": "a.title@text",
              "ruleSearchNoteUrl": "a.title@href"
            }
            """.trimIndent()
        )

        assertEquals(1, sources.size)
        assertEquals("旧源", sources[0].bookSourceName)
        assertTrue(sources[0].enabled)
        assertEquals("div.result", sources[0].ruleSearch?.bookList)
        assertEquals("https://old.example/search?keyword={{key}}&page={{page}}", sources[0].searchUrl)
    }

    @Test
    fun importArraySubscriptionUrlAndYueduOnlineUrl() = runBlocking {
        val newSourceJson = """
            {
              "bookSourceName": "在线源",
              "bookSourceUrl": "https://online.example",
              "searchUrl": "https://online.example/search?q={{key}}"
            }
        """.trimIndent()
        val importer = BookSourceImporter { url ->
            when (url) {
                "https://sub.example/sources.json" -> "[$newSourceJson]"
                else -> null
            }
        }

        val sources = importer.import("""{"sourceUrls":["https://sub.example/sources.json"]}""")
        assertEquals(1, sources.size)
        assertEquals("在线源", sources[0].bookSourceName)

        val yueduText = "yuedu://booksource/importonline?src=" +
            URLEncoder.encode(newSourceJson, "UTF-8")
        val yueduSources = importer.import(yueduText)
        assertEquals("https://online.example", yueduSources[0].bookSourceUrl)
    }

    @Test
    fun importIllegalJsonThrows() = runBlocking {
        val importer = BookSourceImporter { null }

        try {
            importer.import("not a book source")
            fail("invalid source text should throw")
        } catch (error: IllegalArgumentException) {
            assertEquals("书源格式不正确", error.message)
        }
    }
}

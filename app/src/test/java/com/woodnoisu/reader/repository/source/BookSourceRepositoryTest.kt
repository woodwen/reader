package com.woodnoisu.reader.repository.source

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.woodnoisu.reader.model.source.BookSource
import com.woodnoisu.reader.model.source.rule.SearchRule
import com.woodnoisu.reader.network.HtmlService
import com.woodnoisu.reader.persistence.BookSourceDao
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookSourceRepositoryTest {

    @Test
    fun importSourceDisablesUnavailableSources() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse().setBody("<html>ok</html>"))
            server.enqueue(MockResponse().setBody(""))
            val baseUrl = server.url("/").toString()
            val dao = FakeBookSourceDao()
            val repository = BookSourceRepository(dao, HtmlService())

            val result = repository.importSource(
                """
                [
                  {
                    "bookSourceName": "可用源",
                    "bookSourceUrl": "${baseUrl}valid",
                    "enabled": true,
                    "searchUrl": "${baseUrl}valid-search?q={{key}}",
                    "ruleSearch": {
                      "bookList": ".result",
                      "name": ".title@text",
                      "bookUrl": ".title@href"
                    }
                  },
                  {
                    "bookSourceName": "空响应源",
                    "bookSourceUrl": "${baseUrl}empty",
                    "enabled": true,
                    "searchUrl": "${baseUrl}empty-search?q={{key}}",
                    "ruleSearch": {
                      "bookList": ".result",
                      "name": ".title@text",
                      "bookUrl": ".title@href"
                    }
                  }
                ]
                """.trimIndent()
            )

            assertEquals(2, result.total)
            assertEquals(1, result.enabled)
            assertEquals(1, result.disabled)
            assertEquals(1, result.unavailable)
            assertTrue(dao.items.getValue("${baseUrl}valid").enabled)
            assertFalse(dao.items.getValue("${baseUrl}empty").enabled)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun updateEnabledKeepsUnavailableSourceDisabled() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse().setBody(""))
            val baseUrl = server.url("/").toString()
            val source = testSource("${baseUrl}source", "${baseUrl}search?q={{key}}")
            val dao = FakeBookSourceDao()
            dao.insert(source)
            val repository = BookSourceRepository(dao, HtmlService())

            val enabled = repository.updateEnabled(source, true)

            assertFalse(enabled)
            assertFalse(dao.items.getValue(source.bookSourceUrl).enabled)
        } finally {
            server.shutdown()
        }
    }

    private fun testSource(sourceUrl: String, searchUrl: String): BookSource {
        return BookSource(
            bookSourceName = "测试源",
            bookSourceUrl = sourceUrl,
            enabled = false,
            searchUrl = searchUrl,
            ruleSearch = SearchRule(
                bookList = ".result",
                name = ".title@text",
                bookUrl = ".title@href"
            )
        )
    }

    private class FakeBookSourceDao : BookSourceDao {
        val items = linkedMapOf<String, BookSource>()

        override fun liveDataAll(): LiveData<List<BookSource>> {
            return MutableLiveData(items.values.toList())
        }

        override fun liveDataSearch(key: String): LiveData<List<BookSource>> {
            return MutableLiveData(items.values.toList())
        }

        override suspend fun getAllEnabled(): List<BookSource> {
            return items.values.filter { it.enabled }
        }

        override suspend fun getBookSource(key: String): BookSource? {
            return items[key]
        }

        override suspend fun count(): Int {
            return items.size
        }

        override suspend fun maxOrder(): Int {
            var max = 0
            items.values.forEach {
                if (it.customOrder > max) {
                    max = it.customOrder
                }
            }
            return max
        }

        override suspend fun insert(vararg bookSource: BookSource) {
            bookSource.forEach {
                items[it.bookSourceUrl] = it.copy()
            }
        }

        override suspend fun update(vararg bookSource: BookSource) {
            insert(*bookSource)
        }

        override suspend fun delete(vararg bookSource: BookSource) {
            bookSource.forEach {
                items.remove(it.bookSourceUrl)
            }
        }

        override suspend fun delete(key: String) {
            items.remove(key)
        }
    }
}

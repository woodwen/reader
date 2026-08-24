package com.woodnoisu.reader.repository.source

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.woodnoisu.reader.model.source.BookSource
import com.woodnoisu.reader.model.source.rule.BookInfoRule
import com.woodnoisu.reader.model.source.rule.ContentRule
import com.woodnoisu.reader.model.source.rule.ExploreRule
import com.woodnoisu.reader.model.source.rule.SearchRule
import com.woodnoisu.reader.model.source.rule.TocRule
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

    @Test
    fun checkSourceAvailabilityUsesSearchAndClearsFailureMarker() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            enqueueReadableBook(server)
            val source = fullSource(server.url("/").toString()).copy(
                enabled = false,
                bookSourceGroup = "自定义,失效",
                bookSourceComment = "检测失败：旧错误\n用户注释"
            )
            val dao = FakeBookSourceDao()
            dao.insert(source)
            val repository = BookSourceRepository(dao, HtmlService())

            val result = repository.checkSourceAvailability(source, "")

            assertTrue(result.available)
            assertFalse(result.disabled)
            val saved = dao.items.getValue(source.bookSourceUrl)
            assertFalse(saved.enabled)
            assertEquals("自定义", saved.bookSourceGroup)
            assertEquals("用户注释", saved.bookSourceComment)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun checkSourceAvailabilityFallsBackToExploreWhenSearchIsEmpty() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse().setBody("<html><body></body></html>"))
            server.enqueue(searchPage("发现小说", "/book/explore"))
            server.enqueue(detailPage())
            server.enqueue(tocPage())
            server.enqueue(contentPage())
            val source = fullSource(server.url("/").toString())
            val dao = FakeBookSourceDao()
            dao.insert(source)
            val repository = BookSourceRepository(dao, HtmlService())

            val result = repository.checkSourceAvailability(source, "关键词")

            assertTrue(result.available)
            assertTrue(server.takeRequest().path.orEmpty().startsWith("/search"))
            assertTrue(server.takeRequest().path.orEmpty().startsWith("/explore"))
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun checkSourceAvailabilityFailsBeforeNetworkWhenReadRulesMissing() = runBlocking {
        val source = fullSource("https://source.example/").copy(ruleContent = null)
        val dao = FakeBookSourceDao()
        dao.insert(source)
        val repository = BookSourceRepository(dao, HtmlService())

        val result = repository.checkSourceAvailability(source, "关键词")

        assertFalse(result.available)
        assertTrue(result.disabled)
        assertEquals("规则", result.stage)
        assertEquals("书源缺少正文规则", result.message)
        assertFalse(dao.items.getValue(source.bookSourceUrl).enabled)
    }

    @Test
    fun checkSourceAvailabilityDisablesWhenDetailFails() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(searchPage())
            server.enqueue(MockResponse().setBody("<html><body>no detail</body></html>"))
            val source = fullSource(server.url("/").toString())
            val dao = FakeBookSourceDao()
            dao.insert(source)
            val repository = BookSourceRepository(dao, HtmlService())

            val result = repository.checkSourceAvailability(source, "关键词")

            assertFalse(result.available)
            assertTrue(result.disabled)
            assertEquals("详情", result.stage)
            assertFalse(dao.items.getValue(source.bookSourceUrl).enabled)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun checkSourceAvailabilityDisablesWhenTocFails() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(searchPage())
            server.enqueue(detailPage())
            server.enqueue(MockResponse().setBody("<html><body>no toc</body></html>"))
            val source = fullSource(server.url("/").toString())
            val dao = FakeBookSourceDao()
            dao.insert(source)
            val repository = BookSourceRepository(dao, HtmlService())

            val result = repository.checkSourceAvailability(source, "关键词")

            assertFalse(result.available)
            assertTrue(result.disabled)
            assertEquals("目录", result.stage)
            assertFalse(dao.items.getValue(source.bookSourceUrl).enabled)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun checkSourceAvailabilityDisablesAndPreservesUserCommentWhenContentFails() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(searchPage())
            server.enqueue(detailPage())
            server.enqueue(tocPage())
            server.enqueue(MockResponse().setBody("<html><body><div class=\"content\"></div></body></html>"))
            val source = fullSource(server.url("/").toString()).copy(
                bookSourceGroup = "自定义",
                bookSourceComment = "用户注释"
            )
            val dao = FakeBookSourceDao()
            dao.insert(source)
            val repository = BookSourceRepository(dao, HtmlService())

            val result = repository.checkSourceAvailability(source, "关键词")

            assertFalse(result.available)
            assertTrue(result.disabled)
            assertEquals("正文", result.stage)
            val saved = dao.items.getValue(source.bookSourceUrl)
            assertFalse(saved.enabled)
            assertEquals("自定义,失效", saved.bookSourceGroup)
            assertEquals("检测失败：正文：正文内容为空\n用户注释", saved.bookSourceComment)
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

    private fun fullSource(baseUrl: String): BookSource {
        return BookSource(
            bookSourceName = "测试源",
            bookSourceUrl = baseUrl,
            enabled = true,
            searchUrl = "${baseUrl}search?q={{key}}&page={{page}}",
            ruleSearch = SearchRule(
                bookList = ".result",
                name = ".title@text",
                bookUrl = ".title@href"
            ),
            exploreUrl = "${baseUrl}explore?page={{page}}",
            ruleExplore = ExploreRule(
                bookList = ".result",
                name = ".title@text",
                bookUrl = ".title@href"
            ),
            ruleBookInfo = BookInfoRule(
                name = "h1@text",
                tocUrl = ".toc@href"
            ),
            ruleToc = TocRule(
                chapterList = ".chapter",
                chapterName = "text",
                chapterUrl = "href"
            ),
            ruleContent = ContentRule(
                content = ".content@html"
            )
        )
    }

    private fun enqueueReadableBook(server: MockWebServer) {
        server.enqueue(searchPage())
        server.enqueue(detailPage())
        server.enqueue(tocPage())
        server.enqueue(contentPage())
    }

    private fun searchPage(name: String = "测试小说", bookUrl: String = "/book/1"): MockResponse {
        return MockResponse().setBody(
            """
            <html><body>
              <div class="result">
                <a class="title" href="$bookUrl">$name</a>
              </div>
            </body></html>
            """.trimIndent()
        )
    }

    private fun detailPage(): MockResponse {
        return MockResponse().setBody(
            """
            <html><body>
              <h1>测试小说</h1>
              <a class="toc" href="/toc/1">目录</a>
            </body></html>
            """.trimIndent()
        )
    }

    private fun tocPage(): MockResponse {
        return MockResponse().setBody(
            """
            <html><body>
              <a class="chapter" href="/chapter/1">第一章</a>
            </body></html>
            """.trimIndent()
        )
    }

    private fun contentPage(): MockResponse {
        return MockResponse().setBody(
            """
            <html><body>
              <div class="content"><p>正文内容</p></div>
            </body></html>
            """.trimIndent()
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

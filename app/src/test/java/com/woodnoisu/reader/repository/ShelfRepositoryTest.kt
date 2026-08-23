package com.woodnoisu.reader.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.mock
import com.woodnoisu.reader.model.RequestSearchPageByKeyword
import com.woodnoisu.reader.model.source.BookSource
import com.woodnoisu.reader.model.source.rule.BookInfoRule
import com.woodnoisu.reader.model.source.rule.ContentRule
import com.woodnoisu.reader.model.source.rule.SearchRule
import com.woodnoisu.reader.model.source.rule.TocRule
import com.woodnoisu.reader.network.HtmlClient
import com.woodnoisu.reader.network.HtmlService
import com.woodnoisu.reader.persistence.BookSourceDao
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShelfRepositoryTest {

    @Test
    fun fetchRemoteSearchMergesAllEnabledSearchSources() = runBlocking {
        val server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return when {
                    request.path?.startsWith("/one/search") == true -> MockResponse().setBody(
                        searchHtml("第一本", "作者一", "/one/book/1", "玄幻 连载", "第十章", "3200字")
                    )
                    request.path?.startsWith("/two/search") == true -> MockResponse().setBody(
                        searchHtml("第二本", "作者二", "/two/book/1", "都市 完结", "最终章", "4100字")
                    )
                    else -> MockResponse().setResponseCode(404)
                }
            }
        }
        server.start()
        try {
            val sourceOne = testSource(
                name = "源一",
                sourceUrl = server.url("/one/").toString(),
                searchUrl = server.url("/one/search?q={{key}}&page={{page}}").toString()
            )
            val sourceTwo = testSource(
                name = "源二",
                sourceUrl = server.url("/two/").toString(),
                searchUrl = server.url("/two/search?q={{key}}&page={{page}}").toString()
            )
            val repository = ShelfRepository(
                htmlClient = HtmlClient(HtmlService(), FakeBookSourceDao(listOf(sourceOne, sourceTwo))),
                bookDao = mock(),
                bookSignDao = mock(),
                chapterDao = mock(),
                readRecordDao = mock()
            )
            val errors = arrayListOf<String>()

            val responses = repository.fetchRemoteSearch(
                request = RequestSearchPageByKeyword(shopName = "", keyword = "test", page = 1),
                onSuccess = {},
                onError = { errors.add(it) }
            ).toList()
            val response = responses.last()

            assertTrue(responses.isNotEmpty())
            assertEquals(2, response.bookBeans.size)
            assertEquals(listOf("第一本", "第二本"), response.bookBeans.map { it.name }.sorted())
            assertEquals(setOf(sourceOne.bookSourceUrl, sourceTwo.bookSourceUrl), response.bookBeans.map { it.shopName }.toSet())
            val firstBook = response.bookBeans.first { it.name == "第一本" }
            assertEquals(sourceOne.bookSourceUrl, firstBook.shopName)
            assertEquals("源一", firstBook.sourceDisplayName)
            assertEquals("玄幻 连载", firstBook.category)
            assertEquals("连载", firstBook.status)
            assertEquals("第十章", firstBook.latestChapter)
            assertEquals("3200字", firstBook.wordCountText)
            assertTrue(errors.isEmpty())
        } finally {
            server.shutdown()
        }
    }

    private fun searchHtml(
        name: String,
        author: String,
        bookUrl: String,
        kind: String,
        lastChapter: String,
        wordCount: String
    ): String {
        return """
            <html><body>
              <div class="result">
                <a class="title" href="$bookUrl">$name</a>
                <span class="author">$author</span>
                <span class="kind">$kind</span>
                <span class="last">$lastChapter</span>
                <span class="words">$wordCount</span>
                <p class="intro">简介</p>
              </div>
            </body></html>
        """.trimIndent()
    }

    private fun testSource(name: String, sourceUrl: String, searchUrl: String): BookSource {
        return BookSource(
            bookSourceName = name,
            bookSourceUrl = sourceUrl,
            enabled = true,
            searchUrl = searchUrl,
            ruleSearch = SearchRule(
                bookList = ".result",
                name = ".title@text",
                author = ".author@text",
                intro = ".intro@text",
                kind = ".kind@text",
                lastChapter = ".last@text",
                wordCount = ".words@text",
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

    private class FakeBookSourceDao(
        sources: List<BookSource>
    ) : BookSourceDao {
        private val items = sources.associateBy { it.bookSourceUrl }.toMutableMap()

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
            return items.values.maxOfOrNull { it.customOrder } ?: 0
        }

        override suspend fun insert(vararg bookSource: BookSource) {
            bookSource.forEach { items[it.bookSourceUrl] = it }
        }

        override suspend fun update(vararg bookSource: BookSource) {
            insert(*bookSource)
        }

        override suspend fun delete(vararg bookSource: BookSource) {
            bookSource.forEach { items.remove(it.bookSourceUrl) }
        }

        override suspend fun delete(key: String) {
            items.remove(key)
        }
    }
}

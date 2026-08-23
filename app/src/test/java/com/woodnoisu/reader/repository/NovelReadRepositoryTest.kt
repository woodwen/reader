package com.woodnoisu.reader.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.mock
import com.woodnoisu.reader.model.ChapterBean
import com.woodnoisu.reader.model.source.BookSource
import com.woodnoisu.reader.model.source.rule.ContentRule
import com.woodnoisu.reader.network.HtmlClient
import com.woodnoisu.reader.network.HtmlService
import com.woodnoisu.reader.persistence.BookSourceDao
import com.woodnoisu.reader.persistence.ChapterDao
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NovelReadRepositoryTest {

    @Test
    fun getChapterContentsReturnsErrorWhenAllFetchedContentIsBlank() = runBlocking {
        val server = MockWebServer()
        server.enqueue(
            MockResponse().setBody(
                """
                <html><body>
                  <div class="other">没有正文</div>
                </body></html>
                """.trimIndent()
            )
        )
        server.start()
        try {
            val source = BookSource(
                bookSourceName = "空正文源",
                bookSourceUrl = server.url("/").toString(),
                ruleContent = ContentRule(content = ".content@text")
            )
            val repository = NovelReadRepository(
                htmlClient = HtmlClient(HtmlService(), FakeBookSourceDao(listOf(source))),
                bookDao = mock(),
                bookSignDao = mock(),
                chapterDao = FakeChapterDao(),
                readRecordDao = mock()
            )
            val errors = arrayListOf<String>()

            val responses = repository.getChapterContents(
                chapters = listOf(
                    ChapterBean(
                        shopName = source.bookSourceUrl,
                        bookUrl = server.url("/book/1").toString(),
                        url = server.url("/chapter/1").toString(),
                        name = "第一章"
                    )
                ),
                onNext = {},
                onSuccess = {},
                onError = { errors.add(it) }
            ).toList()

            assertTrue(responses.isEmpty())
            assertEquals(listOf("章节正文为空，请换源阅读"), errors)
        } finally {
            server.shutdown()
        }
    }

    private class FakeBookSourceDao(
        sources: List<BookSource>
    ) : BookSourceDao {
        private val items = sources.associateBy { it.bookSourceUrl }.toMutableMap()

        override fun liveDataAll(): LiveData<List<BookSource>> = MutableLiveData(items.values.toList())

        override fun liveDataSearch(key: String): LiveData<List<BookSource>> = MutableLiveData(items.values.toList())

        override suspend fun getAllEnabled(): List<BookSource> = items.values.filter { it.enabled }

        override suspend fun getBookSource(key: String): BookSource? = items[key]

        override suspend fun count(): Int = items.size

        override suspend fun maxOrder(): Int = items.values.maxOfOrNull { it.customOrder } ?: 0

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

    private class FakeChapterDao : ChapterDao {
        override suspend fun get(url: String): ChapterBean? = null

        override suspend fun getContentByUrl(url: String): String? = null

        override suspend fun getListByBookUrl(bookUrl: String, start: Int, limit: Int): MutableList<ChapterBean> =
            mutableListOf()

        override suspend fun getListCountByBookUrl(bookUrl: String): Int = 0

        override suspend fun deleteByBookUrl(bookUrl: String) = Unit

        override suspend fun insert(element: ChapterBean) = Unit

        override suspend fun insertSome(vararg elements: ChapterBean) = Unit

        override suspend fun insertList(list: List<ChapterBean>) = Unit

        override suspend fun update(element: ChapterBean) = Unit

        override suspend fun updateSome(vararg elements: ChapterBean) = Unit

        override suspend fun updateList(elements: List<ChapterBean>) = Unit

        override suspend fun delete(element: ChapterBean) = Unit

        override suspend fun deleteSome(vararg elements: ChapterBean) = Unit

        override suspend fun deleteList(elements: List<ChapterBean>) = Unit
    }
}

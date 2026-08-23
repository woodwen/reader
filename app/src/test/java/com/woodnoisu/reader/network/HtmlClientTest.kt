package com.woodnoisu.reader.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.woodnoisu.reader.model.source.BookSource
import com.woodnoisu.reader.model.source.rule.BookInfoRule
import com.woodnoisu.reader.model.source.rule.ContentRule
import com.woodnoisu.reader.model.source.rule.ExploreRule
import com.woodnoisu.reader.model.source.rule.SearchRule
import com.woodnoisu.reader.model.source.rule.TocRule
import com.woodnoisu.reader.persistence.BookSourceDao
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HtmlClientTest {

    @Test
    fun sourceOptionsSkipEnabledSourcesWithoutSearchOrExploreRules() = runBlocking {
        val dao = FakeBookSourceDao(
            listOf(
                BookSource(
                    bookSourceName = "无规则源",
                    bookSourceUrl = "https://invalid.example",
                    enabled = true
                ),
                BookSource(
                    bookSourceName = "搜索源",
                    bookSourceUrl = "https://search.example",
                    enabled = true,
                    searchUrl = "https://search.example/search?q={{key}}",
                    ruleSearch = SearchRule(bookList = ".result")
                ),
                BookSource(
                    bookSourceName = "发现源",
                    bookSourceUrl = "https://explore.example",
                    enabled = true,
                    exploreUrl = "https://explore.example/list/{{page}}",
                    ruleExplore = ExploreRule(bookList = ".result")
                )
            )
        )
        val client = HtmlClient(HtmlService(), dao)

        val options = client.getSourceOptions()

        assertTrue(options.any { it.name == "全文阅读" })
        assertTrue(options.any { it.key == "https://search.example" && it.dynamic && !it.canExplore })
        assertTrue(options.any { it.key == "https://explore.example" && it.dynamic && it.canExplore })
        assertFalse(options.any { it.key == "https://invalid.example" })
        assertEquals(options.distinctBy { it.key }.size, options.size)
    }

    @Test
    fun dynamicSearchSourceOptionsSkipSourcesThatCannotBeReadByCurrentParser() = runBlocking {
        val readable = readableSearchSource("可读源", "https://readable.example")
        val jsContent = readableSearchSource("JS正文源", "https://js.example").copy(
            ruleContent = ContentRule(content = ".content@js:java.ajax('/chapter')")
        )
        val missingInfo = readableSearchSource("无详情源", "https://missing-info.example").copy(
            ruleBookInfo = BookInfoRule()
        )
        val dao = FakeBookSourceDao(listOf(readable, jsContent, missingInfo))
        val client = HtmlClient(HtmlService(), dao)

        val options = client.getDynamicSearchSourceOptions()

        assertTrue(options.any { it.key == readable.bookSourceUrl })
        assertFalse(options.any { it.key == jsContent.bookSourceUrl })
        assertFalse(options.any { it.key == missingInfo.bookSourceUrl })
    }

    private fun readableSearchSource(name: String, url: String): BookSource {
        return BookSource(
            bookSourceName = name,
            bookSourceUrl = url,
            enabled = true,
            searchUrl = "$url/search?q={{key}}",
            ruleSearch = SearchRule(
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

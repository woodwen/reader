package com.woodnoisu.reader.network.rule

import com.woodnoisu.reader.model.source.BookSource
import com.woodnoisu.reader.model.source.rule.BookInfoRule
import com.woodnoisu.reader.model.source.rule.ContentRule
import com.woodnoisu.reader.model.source.rule.SearchRule
import com.woodnoisu.reader.model.source.rule.TocRule
import com.woodnoisu.reader.network.HtmlService
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleBookParseTest {

    @Test
    fun parseSearchInfoTocAndContentFromHtml() = runBlocking {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(
                MockResponse().setBody(
                    """
                    <html><body>
                      <div class="result">
                        <a class="title" href="/book/1"> 测试小说 </a>
                        <span class="author">作者：张三</span>
                        <span class="kind">玄幻</span>
                        <img class="cover" src="/cover.jpg" />
                        <p class="intro">搜索简介</p>
                      </div>
                    </body></html>
                    """.trimIndent()
                )
            )
            server.enqueue(
                MockResponse().setBody(
                    """
                    <html><body>
                      <h1>测试小说</h1>
                      <span class="author">作者：张三</span>
                      <span class="kind">玄幻</span>
                      <span class="last">第一章</span>
                      <span class="time">今天</span>
                      <img class="cover" src="/detail-cover.jpg" />
                      <p class="intro">详情简介</p>
                      <a class="toc" href="/toc/1">目录</a>
                    </body></html>
                    """.trimIndent()
                )
            )
            server.enqueue(
                MockResponse().setBody(
                    """
                    <html><body>
                      <a class="chapter" href="/chapter/1">第一章 开始</a>
                      <a class="chapter" href="/chapter/2">第二章 继续</a>
                    </body></html>
                    """.trimIndent()
                )
            )
            server.enqueue(
                MockResponse().setBody(
                    """
                    <html><body>
                      <div class="content"><p>正文第一段</p><p>正文第二段</p></div>
                    </body></html>
                    """.trimIndent()
                )
            )

            val baseUrl = server.url("/").toString()
            val parse = RuleBookParse(HtmlService(), testSource(baseUrl))

            val search = parse.getSearchByKeyword("测试", 1)
            assertEquals(1, search.bookBeans.size)
            assertEquals("测试小说", search.bookBeans[0].name)
            assertEquals("张三", search.bookBeans[0].author)
            assertEquals("${baseUrl}book/1", search.bookBeans[0].url)
            assertEquals("${baseUrl}cover.jpg", search.bookBeans[0].cover)

            val info = parse.getBookInfo(search.bookBeans[0].url)!!
            assertEquals("测试小说", info.name)
            assertEquals("${baseUrl}toc/1", info.chaptersUrl)
            assertEquals("今天", info.updateDate)

            val chapters = parse.getChapterList(info.url, info.chaptersUrl, 0, 10)
            assertEquals(2, chapters.size)
            assertEquals("第一章 开始", chapters[0].name)
            assertEquals("${baseUrl}chapter/1", chapters[0].url)
            assertEquals(0, chapters[0].index)

            val content = parse.getChapterContent(chapters[0].url).orEmpty()
            assertTrue(content.contains("正文第一段"))
            assertTrue(content.contains("正文第二段"))
        } finally {
            server.shutdown()
        }
    }

    private fun testSource(baseUrl: String): BookSource {
        return BookSource(
            bookSourceName = "测试源",
            bookSourceUrl = baseUrl,
            searchUrl = "${baseUrl}search?q={{key}}&page={{page}}",
            ruleSearch = SearchRule(
                bookList = ".result",
                name = ".title@text",
                author = ".author@text",
                kind = ".kind@text",
                intro = ".intro@text",
                coverUrl = ".cover@src",
                bookUrl = ".title@href"
            ),
            ruleBookInfo = BookInfoRule(
                name = "h1@text",
                author = ".author@text",
                kind = ".kind@text",
                intro = ".intro@text",
                coverUrl = ".cover@src",
                lastChapter = ".last@text",
                updateTime = ".time@text",
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
}

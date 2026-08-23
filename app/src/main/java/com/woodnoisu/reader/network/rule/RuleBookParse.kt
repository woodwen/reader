package com.woodnoisu.reader.network.rule

import com.woodnoisu.reader.model.BookBean
import com.woodnoisu.reader.model.ChapterBean
import com.woodnoisu.reader.model.ResponseSearchPageByKeyword
import com.woodnoisu.reader.model.ResponseSearchPageByType
import com.woodnoisu.reader.model.source.BookSource
import com.woodnoisu.reader.model.source.rule.BookListRule
import com.woodnoisu.reader.network.HtmlService
import com.woodnoisu.reader.network.parse.HtmlParse

class RuleBookParse(
    htmlService: HtmlService,
    private val source: BookSource
) : HtmlParse(htmlService) {

    override val typeMap: Map<String, String> = if (source.supportsExplore()) {
        mapOf(TYPE_EXPLORE to source.exploreUrl.orEmpty())
    } else {
        emptyMap()
    }

    override suspend fun getSearchByKeyword(keyword: String, page: Int): ResponseSearchPageByKeyword {
        val searchUrl = source.searchUrl
        val books = arrayListOf<BookBean>()
        if (searchUrl.isNullOrBlank()) {
            return ResponseSearchPageByKeyword(keyword, page, page, books)
        }
        val urlRule = SourceUrlRule(searchUrl, keyword, page, source.bookSourceUrl, source)
        val body = urlRule.load(htmlService) ?: return ResponseSearchPageByKeyword(keyword, 0, 0, books)
        books.addAll(parseBookList(body, urlRule.url, source.getSearchRule()))
        return ResponseSearchPageByKeyword(keyword, page, page, books)
    }

    override suspend fun getSearchByType(typeName: String, page: Int): ResponseSearchPageByType {
        val books = arrayListOf<BookBean>()
        val exploreUrl = source.exploreUrl
        if (exploreUrl.isNullOrBlank() || !source.supportsExplore()) {
            return ResponseSearchPageByType(typeName, page, page, books)
        }
        val urlRule = SourceUrlRule(exploreUrl, page = page, baseUrl = source.bookSourceUrl, source = source)
        val body = urlRule.load(htmlService) ?: return ResponseSearchPageByType(typeName, 0, 0, books)
        books.addAll(parseBookList(body, urlRule.url, source.getExploreRule()))
        return ResponseSearchPageByType(typeName, page, page, books)
    }

    override suspend fun getBookInfo(bookUrl: String): BookBean? {
        val urlRule = SourceUrlRule(bookUrl, baseUrl = source.bookSourceUrl, source = source)
        val body = urlRule.load(htmlService) ?: return null
        val analyzer = SourceRuleAnalyzer(body, urlRule.url, urlRule.url)
        val infoRule = source.getBookInfoRule()
        infoRule.init?.takeIf { it.isNotBlank() }?.let {
            analyzer.getElement(it)?.let { element -> analyzer.setContent(element) }
        }
        val name = analyzer.getString(infoRule.name).formatBookName()
        if (name.isBlank()) return null
        val category = analyzer.getString(infoRule.kind)
        val latestChapter = analyzer.getString(infoRule.lastChapter)
        val intro = analyzer.getString(infoRule.intro).formatHtml()
        val tocUrl = analyzer.getString(infoRule.tocUrl, true).ifBlank { bookUrl }
        return BookBean(
            name = name,
            url = bookUrl,
            category = category,
            status = inferBookStatus(category, latestChapter, intro),
            cover = analyzer.getString(infoRule.coverUrl, true),
            author = analyzer.getString(infoRule.author).formatBookAuthor(),
            desc = intro,
            shopName = source.bookSourceUrl,
            chaptersUrl = tocUrl,
            updateDate = analyzer.getString(infoRule.updateTime),
            sourceDisplayName = source.displayName(),
            latestChapter = latestChapter,
            wordCountText = analyzer.getString(infoRule.wordCount)
        )
    }

    override suspend fun getChapterList(
        bookUrl: String,
        chaptersUrl: String,
        startCharter: Int,
        limitCharter: Int
    ): ArrayList<ChapterBean> {
        val targetUrl = chaptersUrl.ifBlank { bookUrl }
        val chapters = arrayListOf<ChapterBean>()
        loadChapterPage(bookUrl, targetUrl, chapters, hashSetOf())
        chapters.forEachIndexed { index, chapter -> chapter.index = index }
        return ArrayList(chapters.drop(startCharter).take(limitCharter))
    }

    override suspend fun getChapterContent(chapterUrl: String): String? {
        val urlRule = SourceUrlRule(chapterUrl, baseUrl = source.bookSourceUrl, source = source)
        val body = urlRule.load(htmlService) ?: return null
        val rule = source.getContentRule()
        val analyzer = SourceRuleAnalyzer(body, urlRule.url, urlRule.url)
        return analyzer.getString(rule.content).formatHtml()
    }

    private suspend fun loadChapterPage(
        bookUrl: String,
        tocUrl: String,
        chapters: MutableList<ChapterBean>,
        visited: MutableSet<String>
    ) {
        if (tocUrl.isBlank() || !visited.add(tocUrl) || visited.size > 10) return
        val urlRule = SourceUrlRule(tocUrl, baseUrl = source.bookSourceUrl, source = source)
        val body = urlRule.load(htmlService) ?: return
        val rule = source.getTocRule()
        val analyzer = SourceRuleAnalyzer(body, urlRule.url, urlRule.url)
        analyzer.getElements(rule.chapterList).forEach { item ->
            val itemAnalyzer = SourceRuleAnalyzer(item, urlRule.url, urlRule.url)
            val title = itemAnalyzer.getString(rule.chapterName)
            if (title.isBlank()) return@forEach
            chapters.add(
                ChapterBean(
                    shopName = source.bookSourceUrl,
                    bookUrl = bookUrl,
                    url = itemAnalyzer.getString(rule.chapterUrl, true).ifBlank { urlRule.url },
                    name = title
                )
            )
        }
        val nextUrl = analyzer.getString(rule.nextTocUrl, true)
        if (nextUrl.isNotBlank() && nextUrl != tocUrl) {
            loadChapterPage(bookUrl, nextUrl, chapters, visited)
        }
    }

    private fun parseBookList(
        body: String,
        baseUrl: String,
        rule: BookListRule
    ): ArrayList<BookBean> {
        val books = arrayListOf<BookBean>()
        val analyzer = SourceRuleAnalyzer(body, baseUrl, baseUrl)
        analyzer.getElements(rule.bookList).forEach { item ->
            val itemAnalyzer = SourceRuleAnalyzer(item, baseUrl, baseUrl)
            val name = itemAnalyzer.getString(rule.name).formatBookName()
            if (name.isBlank()) return@forEach
            val bookUrl = itemAnalyzer.getString(rule.bookUrl, true).ifBlank { baseUrl }
            val category = itemAnalyzer.getString(rule.kind)
            val intro = itemAnalyzer.getString(rule.intro).formatHtml()
            val latestChapter = itemAnalyzer.getString(rule.lastChapter)
            books.add(
                BookBean(
                    name = name,
                    url = bookUrl,
                    category = category,
                    status = inferBookStatus(category, latestChapter, intro),
                    cover = itemAnalyzer.getString(rule.coverUrl, true),
                    author = itemAnalyzer.getString(rule.author).formatBookAuthor(),
                    desc = intro,
                    shopName = source.bookSourceUrl,
                    updateDate = itemAnalyzer.getString(rule.updateTime),
                    sourceDisplayName = source.displayName(),
                    latestChapter = latestChapter,
                    wordCountText = itemAnalyzer.getString(rule.wordCount)
                )
            )
        }
        return books
    }

    private fun String?.formatBookName(): String =
        this.orEmpty().replace(Regex("\\s+作\\s*者.*"), "").trim()

    private fun String?.formatBookAuthor(): String =
        this.orEmpty().replace(Regex(".*?作\\s*?者[:：]"), "").trim()

    private fun String?.formatHtml(): String {
        val html = this.orEmpty()
        if (html.isBlank()) return ""
        return html
            .replace(Regex("</?(?:div|p|br|hr|h\\d|article|dd|dl)[^>]*>", RegexOption.IGNORE_CASE), "\n")
            .replace(Regex("</?[a-zA-Z]+(?=[ >])[^<>]*>"), "")
            .replace(Regex("\\s*\\n+\\s*"), "\n　　")
            .trim()
    }

    private fun inferBookStatus(vararg texts: String): String {
        texts.forEach { value ->
            val text = value.trim()
            if (text.isBlank()) return@forEach
            if (text.contains("连载") || text.contains("未完结")) return "连载"
            if (text.contains("完结") || text.contains("完本") || text.contains("已完结")) return "完结"
        }
        return ""
    }

    companion object {
        const val TYPE_EXPLORE = "发现"
    }
}

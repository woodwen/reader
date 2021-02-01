package com.woodnoisu.reader.network.parse

import com.woodnoisu.reader.model.*
import com.woodnoisu.reader.network.HtmlService
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlin.collections.ArrayList

class QWYDParse(htmlService: HtmlService):
    HtmlParse(htmlService) {

    private val bookShopInfo = BookShopInfo(
        shopName = "全文阅读",
        protocol = "http",
        host = "www.quanwenyuedu.io",
        bookInfoPath = "/n/",
        bookChapterListPath = "xiaoshuo.html",
        bookChapterContentPath = ".html",
        bookSearchByKeyPath = "index.php?c=xs&a=search&keywords=%s&page=%s",
        bookSearchByTypePath = "c/%s-%s.html"
    )

    override val typeMap: Map<String, String> =
        mapOf("奇幻玄幻" to "1",
              "武侠仙侠" to "2",
              "都市官场" to "3",
              "历史军事" to "4",
              "言情穿越" to "5",
              "灵异悬疑" to "6",
              "游戏竞技" to "7",
              "青春耽美" to "8",
              "科幻同人" to "9",
              "经典畅销" to "10",
              "图书杂志" to "11",
              "其他分类" to "0")

    /**
     * 根据关键字搜索
     */
    override suspend fun getSearchByKeyword(keyword: String, page: Int): ResponseSearchPageByKeyword {
        //val host = bookShopInfo.host
        val baseUrl  = "${bookShopInfo.protocol}://${bookShopInfo.host}"
        val url = "${baseUrl}/${String.format(bookShopInfo.bookSearchByKeyPath,keyword,page)}"
        val html = htmlService.getHtml(url,mapOf())
        var currentPage = 0
        var totalPage = 0
        val bookModels = ArrayList<BookBean>()
        var document: Document? = null
        try {
            document = Jsoup.parse(html)
            if (document != null) {
                val pageCountTxt = document.select(".list_page span")[1].html()
                val arr = pageCountTxt.split("/")
                currentPage = arr[0].trim().toInt()
                totalPage = arr[1].trim().toInt()

                val list = document.select(".box .top")
                for (it in list) {
                    val coverUrl = it.select("img").attr("src")
                    val a = it.select("h3 a")
                    val name = a.html()
                    val bookUrl = baseUrl + a.attr("href")
                    val p = it.select("p")
                    val bookAuthor = p[0].select("span").html()
                    val desc = p[1].html()

                    val bookModel = BookBean()
                    bookModel.cover = coverUrl
                    bookModel.name = name
                    bookModel.url = bookUrl
                    bookModel.author = bookAuthor
                    bookModel.desc = desc
                    //bookModel.source = host
                    bookModel.shopName = bookShopInfo.shopName
                    bookModels.add(bookModel)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            document?.empty()
        }
        return ResponseSearchPageByKeyword(
            keyword,
            currentPage,
            totalPage,
            bookModels
        )
    }

    /**
     * 根据类型搜索
     */
    override  suspend fun getSearchByType(typeName:String,page: Int): ResponseSearchPageByType {
        val typeIndex = typeMap[typeName]
        val baseUrl  = "${bookShopInfo.protocol}://${bookShopInfo.host}"
        val url = "${baseUrl}/${String.format(bookShopInfo.bookSearchByTypePath,typeIndex,page)}"
        val html = htmlService.getHtml(url, mapOf())

        var currentPage = 0
        var totalPage = 0
        val bookModels = ArrayList<BookBean>()
        var document: Document? = null
        try {
            document = Jsoup.parse(html)
            if (document != null) {
                val pageCountTxt = document.select(".list_page span")[1].html()
                val arr = pageCountTxt.split("/")
                currentPage = arr[0].trim().toInt()
                totalPage = arr[1].trim().toInt()
                val list = document.select(".box .top")
                for (it in list) {

                    val coverUrl = it.select("img").attr("src")
                    val a = it.select("h3 a")
                    val name = a.html()
                    val bookUrl = baseUrl + a.attr("href")
                    val p = it.select("p")
                    val bookAuthor = p[0].select("span").html()
                    val desc = p[1].html()

                    val bookModel = BookBean()
                    bookModel.name = name
                    bookModel.url = bookUrl
                    bookModel.category = typeName
                    //bookModel.typeUrl = url
                    bookModel.cover = coverUrl
                    bookModel.author = bookAuthor
                    bookModel.desc = desc
                    //bookModel.source = host
                    bookModel.shopName = bookShopInfo.shopName
                    bookModels.add(bookModel)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            document?.empty()
        }
        return ResponseSearchPageByType(
            typeName,
            currentPage,
            totalPage,
            bookModels
        )
    }

    /**
     * 获取书籍信息
     */
    override suspend fun getBookInfo(bookUrl:String): BookBean? {
        val chaptersUrl = "${bookUrl}/${bookShopInfo.bookChapterListPath}"
        var html = htmlService.getHtml(bookUrl, mapOf())
        var bookBean: BookBean? = null
        var document: Document? = null
        try {
            document = Jsoup.parse(html)
            if (document != null) {
                val info = document.select(".top")
                val coverUrl = info.select("img").first().attr("src")
                val bookInfo = info.select("p span")
                val bookName = bookInfo[0].html()
                val author = bookInfo[1].html()
                val category = bookInfo[2].html()
                val status = bookInfo[3].html()
                val desc = document.select(".description p").first().html()

                bookBean = BookBean()
                bookBean.name = bookName
                bookBean.url = bookUrl
                bookBean.status = if (status != "完结") "连载" else "完结"
                bookBean.cover = coverUrl
                bookBean.author = author
                bookBean.category = category
                bookBean.desc = desc
                bookBean.updateDate = ""
                //bookBean.source = bookShopInfo.host
                bookBean.chaptersUrl = chaptersUrl
                bookBean.shopName = bookShopInfo.shopName
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            document?.empty()
        }
        return bookBean
    }

    /**
     * 获取章节列表
     */
    override  suspend fun getChapterList(bookUrl: String,
                                         chaptersUrl: String,
                                         startCharter:Int,
                                         limitCharter:Int): ArrayList<ChapterBean> {
        var html = htmlService.getHtml(chaptersUrl,mapOf())
        var chapters = ArrayList<ChapterBean>()
        var document: Document? = null
        try {
            document = Jsoup.parse(html)
            if (document != null) {
                val chapterTags = document.select(".list .list li a")
                //val bookName = document.select(".top .title span").html()
                for ((i, it) in chapterTags.withIndex()) {
                    val url = bookUrl + it.attr("href")
                    val chapterName = it.html()
                    val chapterModel =
                        ChapterBean()
                    //chapterModel.bookName = bookName
                    chapterModel.bookUrl = bookUrl
                    chapterModel.index = i
                    chapterModel.name = chapterName
                    chapterModel.url = url
                    //chapterModel.source = bookShopInfo.host
                    chapterModel.shopName = bookShopInfo.shopName
                    chapters.add(chapterModel)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            document?.empty()

        }
        return chapters
    }

    /**
     * 获取章节内容
     */
    override  suspend fun getChapterContent(chapterUrl: String): String? {
        val html = htmlService.getHtml(chapterUrl,mapOf())
        var content = ""
        var document: Document? = null
        try {
            document = Jsoup.parse(html)
            if (document != null) {
                val paragraphTags = document.select(".articlebody p")
                val stringBuilder = StringBuilder()
                for (p in paragraphTags) {
                    stringBuilder.append("\t\t\t\t").append(p.html()).append("\n\n")
                }
                content = stringBuilder.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            document?.empty()
        }
        return content
    }
}
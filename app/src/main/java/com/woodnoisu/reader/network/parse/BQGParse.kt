package com.woodnoisu.reader.network.parse

import com.woodnoisu.reader.model.*
import com.woodnoisu.reader.network.HtmlService
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import kotlin.collections.ArrayList

class BQGParse(htmlService: HtmlService):
    HtmlParse(htmlService) {

    private val bookShopInfo = BookShopInfo(
        shopName = "笔趣阁",   //书城名字
        protocol = "https",   //协议
        host = "www.biquge.lol",//书城地址
        bookInfoPath = "/n/",//详情后缀
        bookChapterListPath = "index_%s.html",//目录列表后缀
        bookChapterContentPath = ".html",//具体章节后缀
        bookSearchByKeyPath = "ar.php?keyWord=%s",//搜索后缀
        bookSearchByTypePath = "wapsort/%s_%s.html"//搜索类型后缀
    )

    override val typeMap: Map<String, String> =
        mapOf("奇幻玄幻" to "1",
              "武侠仙侠" to "2",
              "都市官场" to "3",
              "历史军事" to "4",
              "言情穿越" to "7",
              "灵异悬疑" to "5",
              "游戏竞技" to "6")

    /**
     * 根据类型搜索
     */
    override suspend fun getSearchByType(typeName:String,page: Int): ResponseSearchPageByType {
        val typeIndex = typeMap[typeName]
        //val host = bookShopInfo.host
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
                val pageCountTxt = document.select(".hd")[0].html()
                val arrTemp = pageCountTxt.split("当前：")
                val arr = arrTemp[arrTemp.count()-1].split("/")
                currentPage = arr[0].trim().toInt()
                totalPage = arr[1].trim().toInt()

                val list = document.select(".txt-list li")
                for (it in list) {
                    val coverUrl = ""
                    val a = it.select(".s2 a")
                    val name = a.html()
                    val bookUrl = baseUrl + a.attr("href")

                    val p = it.select(".s4")
                    val bookAuthor = p.html()
                    val desc = ""

                    val bookModel = BookBean()
                    bookModel.category = typeName
                    //bookModel.typeUrl = url
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
        return ResponseSearchPageByType(
            typeName,
            currentPage,
            totalPage,
            bookModels
        )
    }

    /**
     * 根据关键字搜索
     */
    override suspend fun getSearchByKeyword(keyword: String, page: Int): ResponseSearchPageByKeyword {
        //val host = bookShopInfo.host
        val baseUrl  = "${bookShopInfo.protocol}://${bookShopInfo.host}"
        val url = "${baseUrl}/${String.format(bookShopInfo.bookSearchByKeyPath,keyword)}"
        val html = htmlService.getHtml(url,mapOf())
        val bookModels = ArrayList<BookBean>()
        var document: Document? = null
        try {
            document = Jsoup.parse(html)
            if (document != null) {
                val list = document.select(".txt-list li")
                for (it in list) {
                    val coverUrl = ""
                    val a = it.select(".s2 a")
                    val name = a.html()
                    if(name.isNullOrBlank()) continue
                    val bookUrl = baseUrl + a.attr("href")

                    val p = it.select(".s4")
                    val bookAuthor = p.html()
                    val desc = ""

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
            0,
            1,
            bookModels
        )
    }

    /**
     * 获取书籍信息
     */
    override suspend fun getBookInfo(bookUrl:String): BookBean? {
        var html = htmlService.getHtml(bookUrl, mapOf())
        var bookBean: BookBean? = null
        var document: Document? = null
        try {
            document = Jsoup.parse(html)
            if (document != null) {
                val coverUrl = document.select(".imgbox img").first().attr("src")
                val bookName = document.select(".top h1").html()
                val bookInfo =document.select(".fix p")
                val author = bookInfo[0].html().replace("作者：","")
                val category = bookInfo[1].html().replace("类别：","")
                val status = bookInfo[2].html().replace("状态：","")
                val updateDate = bookInfo[4].html().replace("最后更新：","")
                val desc = document.select(".desc").first().html()

                bookBean = BookBean()
                bookBean.name = bookName
                bookBean.cover = coverUrl
                bookBean.url = bookUrl
                bookBean.author = author
                bookBean.category = category
                bookBean.status = if (status == "连载") "连载" else "完结"
                bookBean.updateDate = updateDate
                bookBean.desc = desc
                bookBean.chaptersUrl = bookUrl
                //bookBean.source = bookShopInfo.host
                bookBean.shopName =bookShopInfo.shopName
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            document?.empty()
            return bookBean
        }
    }

    /**
     * 获取章节列表
     */
    override  suspend fun getChapterList(bookUrl: String,
                                         chaptersUrl: String,
                                         startCharter:Int,
                                         limitCharter:Int): ArrayList<ChapterBean> {
        var html = htmlService.getHtml(bookUrl, mapOf())
        var chapters = ArrayList<ChapterBean>()
        var document: Document? = null
        try {
            document = Jsoup.parse(html)
            if (document != null) {
                //val bookName = document.select(".top h1").html()
                // 需要翻页获取章节列表
                val chapterTags = document.select(".middle select option")
                // 一页 20章
                // 取最后一页，最后一章
                val begin  = startCharter/20
                val end = chapterTags.count()
                if(begin<=end){
                    val it = chapterTags[begin]
                    val chapterUrl =
                        "${bookShopInfo.protocol}://${bookShopInfo.host}${it.attr("value")}"
                    val html2 = htmlService.getHtml(chapterUrl, mapOf())
                    val document2 = Jsoup.parse(html2)
                    if (document2 != null) {
                        val chapterTgs = document2.select(".section-list")[1].select("li a")
                        for ((i, it) in chapterTgs.withIndex()) {
                            val url =
                                "${bookShopInfo.protocol}://${bookShopInfo.host}" + it.attr("href")
                            val chapterName = it.html()
                            val chapterModel = ChapterBean()
                            //chapterModel.bookName = bookName
                            chapterModel.bookUrl = bookUrl
                            chapterModel.index = 20*begin + i
                            chapterModel.name = chapterName
                            chapterModel.url = url
                            //chapterModel.source = bookShopInfo.host
                            chapterModel.shopName = bookShopInfo.shopName
                            chapters.add(chapterModel)
                        }
                        document2.empty()
                    }
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
        val html = htmlService.getHtml(chapterUrl, mapOf())
        var content = ""
        var document: Document? = null
        try {
            document = Jsoup.parse(html)
            if (document != null) {
                val paragraphTags = document.select(".content")
                val stringBuilder = StringBuilder()
                for (p in paragraphTags) {
                    val d = p.html().replace("<br>","")
                    stringBuilder.append("\t\t\t\t").append(d).append("\n\n")
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
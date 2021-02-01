package com.woodnoisu.reader.network

import com.woodnoisu.reader.model.*
import com.woodnoisu.reader.network.parse.BQGParse
import com.woodnoisu.reader.network.parse.HtmlParse
import com.woodnoisu.reader.network.parse.QWYDParse
import javax.inject.Inject
import kotlin.collections.ArrayList

class HtmlClient @Inject constructor(htmlService: HtmlService) {

    private val parseMap: Map<String, HtmlParse> =
        mapOf("全文阅读" to QWYDParse(htmlService),
              "笔趣阁" to BQGParse(htmlService))

    /**
     * 获取网站
     */
    fun getParseArray():List<String>{
        return parseMap.keys.toList()
    }

    /**
     * 获取类型
     */
    fun getTypeArray(shopName:String):List<String>{
        return parseMap[shopName]?.typeMap?.keys!!.toList()
    }

    /**
     * 获取书籍信息
     */
    suspend fun getBookInfo(shopName:String,bookUrl: String): BookBean? {
        val parse = parseMap[shopName]
        return parse?.getBookInfo(bookUrl)
    }

    /**
     * 根据关键字搜索
     */
    suspend fun getSearchByKeyword(shopName:String,keyword: String, page: Int): ResponseSearchPageByKeyword {
        val parse = parseMap[shopName]
        if(parse!=null){
            return parse.getSearchByKeyword(keyword,page)
        }
        return ResponseSearchPageByKeyword()
    }

    /**
     * 根据类型搜索
     */
    suspend fun getSearchByType(
        shopName:String,
        typeName: String,
        page: Int
    ): ResponseSearchPageByType {
        val parse = parseMap[shopName]
        if (parse != null) {
            return parse.getSearchByType(typeName, page)
        }
        return ResponseSearchPageByType()
    }

    /**
     * 获取章节列表
     */
    suspend fun getChapterList(
        shopName:String,
        bookUrl: String,
        chaptersUrl: String,
        startCharter:Int,
        limitCharter:Int
    ): ArrayList<ChapterBean> {
        val parse = parseMap[shopName]
        if (parse != null) {
            return parse.getChapterList(bookUrl, chaptersUrl,startCharter,limitCharter)
        }
        return ArrayList()
    }

    /**
     * 获取章节内容
     */
    suspend fun getChapterContent(shopName:String, chapterUrl: String): String? {
        val parse = parseMap[shopName]
        return parse?.getChapterContent(chapterUrl)
    }
}
package com.woodnoisu.reader.network

import com.woodnoisu.reader.model.*
import com.woodnoisu.reader.model.source.SourceOption
import com.woodnoisu.reader.network.parse.BQGParse
import com.woodnoisu.reader.network.parse.HtmlParse
import com.woodnoisu.reader.network.parse.QWYDParse
import com.woodnoisu.reader.network.rule.RuleBookParse
import com.woodnoisu.reader.persistence.BookSourceDao
import javax.inject.Inject
import kotlin.collections.ArrayList

class HtmlClient @Inject constructor(
    private val htmlService: HtmlService,
    private val bookSourceDao: BookSourceDao
) {

    private val parseMap: Map<String, HtmlParse> =
        mapOf("全文阅读" to QWYDParse(htmlService),
              "笔趣阁" to BQGParse(htmlService))

    /**
     * 获取网站
     */
    fun getParseArray():List<String>{
        return parseMap.keys.toList()
    }

    fun getFixedSourceOptions(): List<SourceOption> {
        return parseMap.keys.map { SourceOption(it, it, false) }
    }

    suspend fun getSourceOptions(): List<SourceOption> {
        return getFixedSourceOptions() + bookSourceDao.getAllEnabled().map {
            SourceOption(it.bookSourceUrl, it.displayName(), true)
        }
    }

    /**
     * 获取类型
     */
    fun getTypeArray(shopName:String):List<String>{
        return parseMap[shopName]?.typeMap?.keys?.toList() ?: listOf("仅搜索")
    }

    /**
     * 获取书籍信息
     */
    suspend fun getBookInfo(shopName:String,bookUrl: String): BookBean? {
        val parse = parseMap[shopName]
        if (parse != null) {
            return parse.getBookInfo(bookUrl)
        }
        return getRuleParse(shopName)?.getBookInfo(bookUrl)
    }

    /**
     * 根据关键字搜索
     */
    suspend fun getSearchByKeyword(shopName:String,keyword: String, page: Int): ResponseSearchPageByKeyword {
        val parse = parseMap[shopName]
        if(parse!=null){
            return parse.getSearchByKeyword(keyword,page)
        }
        getRuleParse(shopName)?.let {
            return it.getSearchByKeyword(keyword, page)
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
        getRuleParse(shopName)?.let {
            return it.getSearchByType(typeName, page)
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
        getRuleParse(shopName)?.let {
            return it.getChapterList(bookUrl, chaptersUrl, startCharter, limitCharter)
        }
        return ArrayList()
    }

    /**
     * 获取章节内容
     */
    suspend fun getChapterContent(shopName:String, chapterUrl: String): String? {
        val parse = parseMap[shopName]
        if (parse != null) {
            return parse.getChapterContent(chapterUrl)
        }
        return getRuleParse(shopName)?.getChapterContent(chapterUrl)
    }

    private suspend fun getRuleParse(sourceUrl: String): RuleBookParse? {
        val source = bookSourceDao.getBookSource(sourceUrl) ?: return null
        if (!source.enabled) return null
        return RuleBookParse(htmlService, source)
    }
}

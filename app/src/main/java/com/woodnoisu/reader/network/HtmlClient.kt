package com.woodnoisu.reader.network

import com.woodnoisu.reader.model.*
import com.woodnoisu.reader.model.source.SourceOption
import com.woodnoisu.reader.network.rule.RuleBookParse
import com.woodnoisu.reader.persistence.BookSourceDao
import javax.inject.Inject
import kotlin.collections.ArrayList

class HtmlClient @Inject constructor(
    private val htmlService: HtmlService,
    private val bookSourceDao: BookSourceDao
) {

    suspend fun getSourceOptions(): List<SourceOption> {
        return bookSourceDao.getAllEnabled()
            .filter { it.supportsExplore() }
            .map {
                SourceOption(
                    key = it.bookSourceUrl,
                    name = it.displayName(),
                    dynamic = true,
                    canExplore = it.supportsExplore()
                )
            }
    }

    suspend fun getDynamicSearchSourceOptions(): List<SourceOption> {
        return bookSourceDao.getAllEnabled().filter { it.supportsReadableSearch() }.map {
            SourceOption(
                key = it.bookSourceUrl,
                name = it.displayName(),
                dynamic = true,
                canExplore = it.supportsExplore()
            )
        }
    }

    suspend fun getSourceDisplayName(shopName: String): String {
        return bookSourceDao.getBookSource(shopName)?.displayName().orEmpty().ifBlank { shopName }
    }

    /**
     * 获取书籍信息
     */
    suspend fun getBookInfo(shopName:String,bookUrl: String): BookBean? {
        return getRuleParse(shopName)?.getBookInfo(bookUrl)?.apply {
            sourceDisplayName = getSourceDisplayName(shopName)
        }
    }

    /**
     * 根据关键字搜索
     */
    suspend fun getSearchByKeyword(shopName:String,keyword: String, page: Int): ResponseSearchPageByKeyword {
        getRuleParse(shopName)?.let {
            return it.getSearchByKeyword(keyword, page).also { response ->
                val displayName = getSourceDisplayName(shopName)
                response.bookBeans.forEach { it.sourceDisplayName = displayName }
            }
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
        getRuleParse(shopName)?.let {
            return it.getChapterList(bookUrl, chaptersUrl, startCharter, limitCharter)
        }
        return ArrayList()
    }

    /**
     * 获取章节内容
     */
    suspend fun getChapterContent(shopName:String, chapterUrl: String): String? {
        return getRuleParse(shopName)?.getChapterContent(chapterUrl)
    }

    private suspend fun getRuleParse(sourceUrl: String): RuleBookParse? {
        val source = bookSourceDao.getBookSource(sourceUrl) ?: return null
        if (!source.enabled) return null
        return RuleBookParse(htmlService, source)
    }
}

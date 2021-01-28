package com.woodnoisu.reader.feature.reader.domain.repository

import com.woodnoisu.reader.feature.reader.domain.model.*

internal interface ReaderRepository {

    /**
     * 获取书籍
     */
    suspend fun getBook(bookId: String): ReaderDomainModel

    /**
     * 获取章节内容（多）
     */
    suspend fun getChapterContents(chapters: List<ReaderChapterDomainModel>): ReaderDomainModel

    /**
     * 获取章节列表
     */
    suspend fun getChapters(readerBookDomainModel: ReaderBookDomainModel,
                                start: Int,
                                limit:Int=100,
                                cacheContents:Boolean=false): ReaderDomainModel

    /**
     * 获取阅读记录
     */
    suspend fun getBookRecord(bookUrl: String): ReaderDomainModel

    /**
     * 获取书签列表
     */
    suspend fun getSigns(bookUrl: String): ReaderDomainModel

    /**
     * 保存阅读记录
     */
    suspend fun saveBookRecord(readerRecordDomainModel: ReaderRecordDomainModel)

    /**
     * 添加书签
     */
    suspend fun addSign(bookUrl: String, chapterUrl: String, chapterName: String)

    /**
     * 删除书签
     */
    suspend fun deleteSigns(readerBookSignDomainModels: List<ReaderBookSignDomainModel>)
}

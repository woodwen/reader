package com.woodnoisu.reader.feature.reader.domain.repository

import com.woodnoisu.reader.feature.reader.domain.model.*
import kotlinx.coroutines.flow.Flow

internal interface ReaderRepository {

    /**
     * 获取书籍
     */
    suspend fun getBook(request: RequestGetBookDomainModel,
                        onSuccess: (String) -> Unit,
                        onError: (String) -> Unit): Flow<ResponseGetBookDomainModel>

    /**
     * 获取章节内容（多）
     */
    suspend fun getChapterContents(request: RequestGetChapterContentsDomainModel,
                                   onNext: (Int) -> Unit,
                                   onSuccess: (String) -> Unit,
                                   onError: (String) -> Unit): Flow<ResponseGetChapterContentsDomainModel>

    /**
     * 获取章节列表
     */
    suspend fun getChapters(request: RequestGetChaptersDomainModel,
                            onSuccess: (String) -> Unit,
                            onError: (String) -> Unit): Flow<ResponseGetChaptersDomainModel>

    /**
     * 获取阅读记录
     */
    suspend fun getBookRecord(request: RequestGetBookRecordDomainModel,
                              onSuccess: (String) -> Unit,
                              onError: (String) -> Unit): Flow<ResponseGetBookRecordDomainModel>

    /**
     * 获取书签列表
     */
    suspend fun getSigns(request: RequestGetSignsDomainModel,
                         onSuccess: (String) -> Unit,
                         onError: (String) -> Unit):  Flow<ResponseGetSignsDomainModel>

    /**
     * 保存阅读记录
     */
    suspend fun setBookRecord(request: RequestSetBookRecordDomainModel,
                              onSuccess: (String) -> Unit,
                              onError: (String) -> Unit): Flow<ResponseSetBookRecordDomainModel>

    /**
     * 添加书签
     */
    suspend fun addSign(request: RequestAddSignDomainModel,
                        onSuccess: (String) -> Unit,
                        onError: (String) -> Unit): Flow<ResponseAddSignDomainModel>

    /**
     * 删除书签
     */
    suspend fun deleteSigns(request: RequestDeleteSignsDomainModel,
                            onSuccess: (String) -> Unit,
                            onError: (String) -> Unit): Flow<ResponseDeleteSignsDomainModel>
}

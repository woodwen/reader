package com.woodnoisu.reader.repository

import androidx.annotation.WorkerThread
import com.woodnoisu.reader.model.BookBean
import com.woodnoisu.reader.model.RequestBookInfo
import com.woodnoisu.reader.model.RequestSearchPageByKeyword
import com.woodnoisu.reader.model.ResponseBookInfo
import com.woodnoisu.reader.model.ResponseSearchPageByKeyword
import com.woodnoisu.reader.network.HtmlClient
import com.woodnoisu.reader.persistence.BookDao
import com.woodnoisu.reader.persistence.BookSignDao
import com.woodnoisu.reader.persistence.ChapterDao
import com.woodnoisu.reader.persistence.ReadRecordDao
import com.woodnoisu.reader.utils.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.Executors
import javax.inject.Inject

/**
 * 搜索存储器
 */
class ShelfRepository @Inject constructor(
    private val htmlClient: HtmlClient,
    private val bookDao: BookDao,
    private val bookSignDao: BookSignDao,
    private val chapterDao: ChapterDao,
    private val readRecordDao: ReadRecordDao
):Repository {
    companion object {
        private const val TAG = "ShelfRepository"
        private const val ERROR_NO_SOURCE = "没有可用导入书源"
        private const val ERROR_SOURCE_SEARCH = "书源搜索暂时不可用，请稍后重试"
        private const val ERROR_BOOK_INFO = "书籍详情暂时加载失败，请稍后重试"
        private const val MAX_SEARCH_THREADS = 8
        private const val SOURCE_SEARCH_TIMEOUT_MS = 15000L
    }

    /**
     * 填充书架
     */
    @WorkerThread
    suspend fun fetchBookList(
        keyword: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    )= flow {
        try {
            val bookList = if(keyword.isNullOrBlank()){
                //没有关键字搜索，则显示全部
                bookDao.getListByFavorite()
            }else{
                //有关键字搜索，显示搜索内容
                bookDao.getListByName(keyword)
            }
            emit(bookList)
            onSuccess("获取成功")
        }catch (e:Exception){
            onError(e.toString())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 取消订阅书籍
     */
    @WorkerThread
    suspend fun deleteBook(
        book: BookBean,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    )= flow {
        try {
            if (book != null) {
                book.favorite = 0
                bookDao.update(book)
            }
            emit(book)
            onSuccess("删除成功")
        } catch (e: Exception) {
            onError(e.toString())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 订阅书籍
     */
    @WorkerThread
    suspend fun insertBook(
        book: BookBean,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    )= flow {
        try {
            if (book != null) {
                book.favorite = 1
                bookDao.insert(book)
            }
            emit(book)
            onSuccess("加入书架成功")
        } catch (e: Exception) {
            onError(e.toString())
        }
    }.flowOn(Dispatchers.IO)

    @WorkerThread
    suspend fun fetchRemoteSearch(
        request: RequestSearchPageByKeyword,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) = channelFlow {
        try {
            if (request.keyword.isBlank()) {
                onError("关键字不能为空")
                return@channelFlow
            }

            val sources = htmlClient.getDynamicSearchSourceOptions()
            if (sources.isEmpty()) {
                onError(ERROR_NO_SOURCE)
                return@channelFlow
            }

            val searchDispatcher = Executors.newFixedThreadPool(
                minOf(MAX_SEARCH_THREADS, sources.size)
            ).asCoroutineDispatcher()
            val mutex = Mutex()
            val bookMap = linkedMapOf<String, BookBean>()
            var responseCount = 0
            var emitted = false
            var successNotified = false
            try {
                coroutineScope {
                    sources.forEach { source ->
                        launch(searchDispatcher) {
                            val response = try {
                                withTimeoutOrNull(SOURCE_SEARCH_TIMEOUT_MS) {
                                    htmlClient.getSearchByKeyword(source.key, request.keyword, request.page)
                                }
                            } catch (e: Exception) {
                                LogUtil.e(TAG, "书架远程书源搜索失败：${source.name}", e)
                                null
                            }
                            var notifySuccess = false
                            mutex.withLock {
                                if (response != null && response.currentPage != 0 && response.totalPage != 0) {
                                    responseCount++
                                } else if (response == null) {
                                    LogUtil.e(TAG, "书架远程书源搜索超时或失败：${source.name}")
                                }
                                response?.bookBeans?.forEach { book ->
                                    if (book.sourceDisplayName.isBlank()) {
                                        book.sourceDisplayName = source.name
                                    }
                                    bookMap["${book.shopName}|${book.url}"] = book
                                }
                                if (bookMap.isNotEmpty()) {
                                    emitted = true
                                    if (!successNotified) {
                                        successNotified = true
                                        notifySuccess = true
                                    }
                                    send(
                                        ResponseSearchPageByKeyword(
                                            keyword = request.keyword,
                                            currentPage = request.page,
                                            totalPage = request.page,
                                            bookBeans = ArrayList(bookMap.values)
                                        )
                                    )
                                }
                            }
                            if (notifySuccess) {
                                onSuccess("获取成功")
                            }
                        }
                    }
                }
            } finally {
                searchDispatcher.close()
            }
            if (!emitted && responseCount == 0) {
                onError(ERROR_SOURCE_SEARCH)
            } else if (!emitted) {
                onError("没有找到相关书籍，换个关键词试试")
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "书架远程书源搜索失败", e)
            onError(ERROR_SOURCE_SEARCH)
        }
    }.flowOn(Dispatchers.IO)

    @WorkerThread
    suspend fun fetchBookInfo(
        request: RequestBookInfo,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) = flow {
        try {
            if (request.bookUrl.isBlank()) {
                onError("获取失败，书籍链接为空")
                return@flow
            }
            val sourceDisplayName = htmlClient.getSourceDisplayName(request.shopName)
            val book = bookDao.getByFavoriteAndUrl(request.bookUrl)
            if (book != null) {
                book.sourceDisplayName = sourceDisplayName
                emit(ResponseBookInfo(book))
                onSuccess("获取书籍信息成功")
                return@flow
            }
            val remoteBook = htmlClient.getBookInfo(request.shopName, request.bookUrl)
            if (remoteBook != null && remoteBook.name.isNotBlank() && remoteBook.url.isNotBlank()) {
                if (remoteBook.sourceDisplayName.isBlank()) {
                    remoteBook.sourceDisplayName = sourceDisplayName
                }
                val temp = bookDao.getByUrl(remoteBook.url)
                if (temp != null) {
                    remoteBook.id = temp.id
                }
                bookDao.insert(remoteBook)
                emit(ResponseBookInfo(remoteBook))
                onSuccess("获取书籍信息成功")
            } else {
                onError(ERROR_BOOK_INFO)
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "书架获取远程书籍详情失败", e)
            onError(ERROR_BOOK_INFO)
        }
    }.flowOn(Dispatchers.IO)
}

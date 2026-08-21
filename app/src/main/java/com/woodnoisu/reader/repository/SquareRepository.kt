package com.woodnoisu.reader.repository

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import com.woodnoisu.reader.model.*
import com.woodnoisu.reader.model.source.SourceOption
import com.woodnoisu.reader.network.HtmlClient
import com.woodnoisu.reader.persistence.BookDao
import com.woodnoisu.reader.utils.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * 搜索存储器
 */
class SquareRepository @Inject constructor(
    private val htmlClient: HtmlClient,
    private val bookDao: BookDao
):Repository {
    companion object {
        private const val TAG = "SquareRepository"
        private const val ERROR_SHOP_LOAD = "书城暂时连接不上，请检查网络后下拉重试"
        private const val ERROR_EMPTY_RESULT = "没有找到相关书籍，换个分类或关键词试试"
        private const val ERROR_BOOK_INFO = "书籍详情暂时加载失败，请稍后重试"
        private const val ERROR_ADD_BOOK = "加入书架失败，请稍后重试"
    }

    /**
     * 按类型搜索
     */
    @WorkerThread
    suspend fun fetchSearchType(
        request: RequestSearchPageByType,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) = flow {
        try {
            val shopName = request.shopName
            val page = request.page
            val typeName = request.typeName
            if (!typeName.isBlank()) {
                val responseSearch =
                    htmlClient.getSearchByType(shopName, typeName, page)
                if (!responseSearch.bookBeans.isNullOrEmpty()) {
                    emit(responseSearch)
                    onSuccess("获取成功")
                } else if (responseSearch.currentPage == 0 && responseSearch.totalPage == 0) {
                    onError(ERROR_SHOP_LOAD)
                } else {
                    onError(ERROR_EMPTY_RESULT)
                }
            } else {
                onError("书城分类暂时不可用，请切换分类后重试")
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "按类型搜索失败", e)
            onError(ERROR_SHOP_LOAD)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 按关键字搜索
     */
    @WorkerThread
    suspend fun fetchSearchKeyWord(
        request: RequestSearchPageByKeyword,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) = flow {
        try {
            var shopName = request.shopName
            var keyword = request.keyword
            val page = request.page
            if (keyword.isBlank()) {
                keyword = ""
            }
            val responseSearch =
                htmlClient.getSearchByKeyword(shopName, keyword, page)
            if (!responseSearch.bookBeans.isNullOrEmpty()) {
                emit(responseSearch)
                onSuccess("获取成功")
            } else if (responseSearch.currentPage == 0 && responseSearch.totalPage == 0) {
                onError(ERROR_SHOP_LOAD)
            } else {
                onError(ERROR_EMPTY_RESULT)
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "按关键字搜索失败", e)
            onError(ERROR_SHOP_LOAD)
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
    ) = flow {
        try {
            if (!book.url.isBlank()) {
                var oldFav = 0
                //先从数据库里查询是否已存在
                val temp = bookDao.getByUrl(book.url)
                if (temp != null) {
                    book.id = temp.id
                    oldFav = temp.favorite
                }
                book.favorite = 1
                bookDao.insert(book)
                emit(book)
                if (oldFav == 1) {
                    onError("已在书架中无需再次加入")
                } else {
                    onSuccess("加入书架成功")
                }
            } else {
                onError("添加失败，需要添加的书籍或者书籍链接为空")
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "加入书架失败", e)
            onError(ERROR_ADD_BOOK)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 获取书籍内容
     */
    @WorkerThread
    suspend fun fetchBookInfo(
        request: RequestBookInfo,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) = flow {
        try {
            val shopName = request.shopName
            val bookUrl = request.bookUrl
            if (bookUrl.isNotBlank()) {
                // 从数据库中获取书籍
                val book = bookDao.getByFavoriteAndUrl(bookUrl)
                if (book != null) {
                    // 数据库获取成功
                    emit(ResponseBookInfo(book))
                    onSuccess("获取书籍信息成功")
                } else {
                    //数据库获取失败，从网络获取
                    val remoteBook = htmlClient.getBookInfo(shopName, bookUrl)
                    if (remoteBook != null) {
                        // 如果获取成功则缓存到数据库
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
                }
            } else {
                onError("获取失败，书籍链接为空")
            }
        } catch (e: Exception) {
            LogUtil.e(TAG, "获取书籍信息失败", e)
            onError(ERROR_BOOK_INFO)
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 获取网站类型
     */
    @MainThread
    fun getTypes(shopName:String): List<String> {
        return htmlClient.getTypeArray(shopName)
    }

    /**
     * 获取固定书城
     */
    @MainThread
    fun getParses(): List<String> {
        return htmlClient.getParseArray()
    }

    @MainThread
    fun getFixedSourceOptions(): List<SourceOption> {
        return htmlClient.getFixedSourceOptions()
    }

    @WorkerThread
    suspend fun fetchSourceOptions() = flow {
        emit(htmlClient.getSourceOptions())
    }.flowOn(Dispatchers.IO)
}

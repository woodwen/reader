package com.woodnoisu.reader.repository

import androidx.annotation.WorkerThread
import com.woodnoisu.reader.model.BookBean
import com.woodnoisu.reader.persistence.BookDao
import com.woodnoisu.reader.persistence.BookSignDao
import com.woodnoisu.reader.persistence.ChapterDao
import com.woodnoisu.reader.persistence.ReadRecordDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/**
 * 搜索存储器
 */
class ShelfRepository @Inject constructor(
    private val bookDao: BookDao,
    private val bookSignDao: BookSignDao,
    private val chapterDao: ChapterDao,
    private val readRecordDao: ReadRecordDao
):Repository {
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
}
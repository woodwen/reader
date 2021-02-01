package com.woodnoisu.reader.persistence

import androidx.room.*
import com.woodnoisu.reader.model.BookSignBean

@Dao
interface BookSignDao:
    BaseBeanDao<BookSignBean> {
    @Query("SELECT * FROM my_signs WHERE `chapterUrl`== :chapterUrl")
    suspend fun getByChapterUrl(chapterUrl: String): BookSignBean?

    @Query("SELECT * FROM my_signs")
    suspend fun getList(): List<BookSignBean>

    @Query("SELECT * FROM my_signs WHERE `bookUrl`== :bookUrl")
    suspend fun getListByBookUrl(bookUrl: String): MutableList<BookSignBean>
}

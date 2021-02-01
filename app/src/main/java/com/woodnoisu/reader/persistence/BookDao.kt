package com.woodnoisu.reader.persistence

import androidx.room.*
import com.woodnoisu.reader.model.BookBean

@Dao
interface BookDao: BaseBeanDao<BookBean> {
    @Query("SELECT * FROM my_shelf WHERE url = :bookUrl")
    suspend fun getByUrl(bookUrl: String): BookBean?

    @Query("SELECT * FROM my_shelf WHERE url = :bookUrl AND `favorite`==1")
    suspend fun getByFavoriteAndUrl(bookUrl: String): BookBean?

    @Query("SELECT * FROM my_shelf")
    suspend fun getList(): List<BookBean>

    @Query("SELECT * FROM my_shelf WHERE `favorite`== 1")
    suspend fun getListByFavorite(): List<BookBean>

    @Query("SELECT * FROM my_shelf WHERE name IN (:names)")
    suspend fun getListByNames(names: List<String>): List<BookBean>

    @Query("SELECT * FROM my_shelf WHERE `favorite`== 1 and name LIKE '%' || :bookName || '%'")
    suspend fun getListByName(bookName: String): List<BookBean>
}

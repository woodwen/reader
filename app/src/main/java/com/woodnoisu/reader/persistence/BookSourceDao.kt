package com.woodnoisu.reader.persistence

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.woodnoisu.reader.model.source.BookSource

@Dao
interface BookSourceDao {

    @Query("SELECT * FROM book_sources ORDER BY customOrder ASC, bookSourceName ASC")
    fun liveDataAll(): LiveData<List<BookSource>>

    @Query("SELECT * FROM book_sources WHERE bookSourceName LIKE :key OR bookSourceGroup LIKE :key OR bookSourceUrl LIKE :key OR bookSourceComment LIKE :key ORDER BY customOrder ASC, bookSourceName ASC")
    fun liveDataSearch(key: String): LiveData<List<BookSource>>

    @Query("SELECT * FROM book_sources WHERE enabled = 1 ORDER BY customOrder ASC, bookSourceName ASC")
    suspend fun getAllEnabled(): List<BookSource>

    @Query("SELECT * FROM book_sources WHERE bookSourceUrl = :key")
    suspend fun getBookSource(key: String): BookSource?

    @Query("SELECT COUNT(*) FROM book_sources")
    suspend fun count(): Int

    @Query("SELECT COALESCE(MAX(customOrder), 0) FROM book_sources")
    suspend fun maxOrder(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg bookSource: BookSource)

    @Update
    suspend fun update(vararg bookSource: BookSource)

    @Delete
    suspend fun delete(vararg bookSource: BookSource)

    @Query("DELETE FROM book_sources WHERE bookSourceUrl = :key")
    suspend fun delete(key: String)
}

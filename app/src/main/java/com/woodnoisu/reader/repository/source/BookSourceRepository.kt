package com.woodnoisu.reader.repository.source

import androidx.lifecycle.LiveData
import com.woodnoisu.reader.model.source.BookSource
import com.woodnoisu.reader.network.HtmlService
import com.woodnoisu.reader.persistence.BookSourceDao
import javax.inject.Inject

class BookSourceRepository @Inject constructor(
    private val bookSourceDao: BookSourceDao,
    htmlService: HtmlService
) {
    private val importer = BookSourceImporter { url ->
        htmlService.getHtml(url, emptyMap())
    }

    fun liveData(searchKey: String?): LiveData<List<BookSource>> {
        val key = searchKey?.trim().orEmpty()
        return if (key.isBlank()) {
            bookSourceDao.liveDataAll()
        } else {
            bookSourceDao.liveDataSearch("%$key%")
        }
    }

    suspend fun getSource(key: String): BookSource? {
        return bookSourceDao.getBookSource(key)
    }

    suspend fun importSource(text: String): Int {
        val sources = importer.import(text)
        insertSources(sources)
        return sources.size
    }

    suspend fun parseSources(text: String): List<BookSource> {
        return importer.import(text)
    }

    suspend fun saveSource(source: BookSource, oldKey: String? = null) {
        if (source.bookSourceUrl.isBlank()) {
            throw IllegalArgumentException("书源地址不能为空")
        }
        if (source.bookSourceName.isBlank()) {
            throw IllegalArgumentException("书源名称不能为空")
        }
        if (!oldKey.isNullOrBlank() && oldKey != source.bookSourceUrl) {
            bookSourceDao.delete(oldKey)
        }
        source.lastUpdateTime = System.currentTimeMillis()
        if (source.customOrder == 0) {
            source.customOrder = bookSourceDao.maxOrder() + 1
        }
        bookSourceDao.insert(source)
    }

    suspend fun delete(source: BookSource) {
        bookSourceDao.delete(source)
    }

    suspend fun updateEnabled(source: BookSource, enabled: Boolean) {
        bookSourceDao.update(source.copy(enabled = enabled))
    }

    fun toJson(source: BookSource): String {
        return BookSource.gson.toJson(source)
    }

    private suspend fun insertSources(sources: List<BookSource>) {
        var order = bookSourceDao.maxOrder()
        sources.forEach { source ->
            val old = bookSourceDao.getBookSource(source.bookSourceUrl)
            if (old != null && source.customOrder == 0) {
                source.customOrder = old.customOrder
            } else if (source.customOrder == 0) {
                source.customOrder = ++order
            }
            if (source.lastUpdateTime == 0L) {
                source.lastUpdateTime = System.currentTimeMillis()
            }
        }
        if (sources.isNotEmpty()) {
            bookSourceDao.insert(*sources.toTypedArray())
        }
    }
}

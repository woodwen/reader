package com.woodnoisu.reader.feature.album.data.repository

import com.woodnoisu.reader.feature.album.data.model.toBean
import com.woodnoisu.reader.feature.album.data.model.toDomainModel

import com.woodnoisu.reader.feature.album.domain.model.AlbumBookDomainModel
import com.woodnoisu.reader.feature.album.domain.model.AlbumDomainModel
import com.woodnoisu.reader.feature.album.domain.repository.AlbumRepository
import com.woodnoisu.reader.library.base.network.HtmlClient
import com.woodnoisu.reader.library.base.persistence.BookDao

internal class AlbumRepositoryImpl(
    private val htmlClient: HtmlClient,
    private val bookDao: BookDao
    ): AlbumRepository {

    override suspend fun getBookListByType(shopName:String,typeName: String, page: Int): AlbumDomainModel {
        var currentPage = 1
        var totalPage = 1
        var albumBookDomainModels: List<AlbumBookDomainModel>? = listOf()

        if (typeName.isNotBlank()) {
            val responseSearch =
                    htmlClient.getSearchByType(shopName, typeName, page)
            if (!responseSearch.bookBeans.isNullOrEmpty()) {
                currentPage = responseSearch.currentPage
                totalPage = responseSearch.totalPage
                albumBookDomainModels = responseSearch.bookBeans.map { it.toDomainModel() }
            }
        }

        return AlbumDomainModel(currentPage = currentPage,
                                totalPage = totalPage,
                                albumBookDomainModels = albumBookDomainModels)
    }

    override suspend fun getBookListByKeyword(shopName:String,keyword: String, page: Int): AlbumDomainModel {
        var currentPage = 1
        var totalPage = 1
        var albumBookDomainModels: List<AlbumBookDomainModel>? = listOf()

        val responseSearch =
                htmlClient.getSearchByKeyword(shopName, keyword, page)
        if (!responseSearch.bookBeans.isNullOrEmpty()) {
            currentPage = responseSearch.currentPage
            totalPage = responseSearch.totalPage
            albumBookDomainModels = responseSearch.bookBeans.map { it.toDomainModel() }
        }
        return AlbumDomainModel(currentPage = currentPage,
                                totalPage = totalPage,
                                albumBookDomainModels = albumBookDomainModels)
    }

    override suspend fun getBook(shopName:String,bookUrl: String): AlbumDomainModel {
        var albumBookDomainModel: AlbumBookDomainModel? = null
        if (bookUrl.isNotBlank()) {
            // 从数据库中获取书籍
            val book = bookDao.getByFavoriteAndUrl(bookUrl)
            if (book != null) {
                // 数据库获取成功
                return AlbumDomainModel(albumBookDomainModel = book.toDomainModel())
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
                    albumBookDomainModel = remoteBook.toDomainModel()
                }
            }
        }
        return AlbumDomainModel(albumBookDomainModel = albumBookDomainModel)
    }

    override suspend fun insertBook(albumBook: AlbumBookDomainModel) {
        if (albumBook.url.isNotBlank()) {
            //先从数据库里查询是否已存在
            val temp = bookDao.getByUrl(albumBook.url)
            if (temp != null) {
                albumBook.id = temp.id
            }
            albumBook.favorite = 1
            bookDao.insert(albumBook.toBean())
        }
    }

    override fun getTypes(shopName: String): List<String> {
        return htmlClient.getTypeArray(shopName)
    }

    override fun getParses(): List<String> {
        return htmlClient.getParseArray()
    }
}
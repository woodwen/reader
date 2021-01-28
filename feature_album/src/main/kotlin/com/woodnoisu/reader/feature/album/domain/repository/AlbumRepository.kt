package com.woodnoisu.reader.feature.album.domain.repository

import com.woodnoisu.reader.feature.album.domain.model.AlbumBookDomainModel
import com.woodnoisu.reader.feature.album.domain.model.AlbumDomainModel

internal interface AlbumRepository {

    suspend fun getBookListByKeyword(
        shopName: String,
        keyword: String,
        page: Int
    ): AlbumDomainModel

    suspend fun getBookListByType(
        shopName: String,
        typeName: String,
        page: Int
    ): AlbumDomainModel

    suspend fun getBook(shopName: String, bookUrl: String): AlbumDomainModel

    suspend fun insertBook(albumBook: AlbumBookDomainModel)

    fun getTypes(shopName: String): List<String>

    fun getParses(): List<String>
}

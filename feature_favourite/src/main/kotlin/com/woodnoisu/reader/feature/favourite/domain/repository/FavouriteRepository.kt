package com.woodnoisu.reader.feature.favourite.domain.repository

import com.woodnoisu.reader.feature.favourite.domain.model.FavouriteBookDomainModel
import com.woodnoisu.reader.feature.favourite.domain.model.FavouriteDomainModel

internal interface FavouriteRepository {

    suspend fun getBookList(keyword: String): FavouriteDomainModel

    suspend fun insertBook(modelFavourite: FavouriteBookDomainModel)

    suspend fun deleteBook(modelFavourite: FavouriteBookDomainModel)
}

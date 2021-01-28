package com.woodnoisu.reader.feature.favourite.data.repository

import com.woodnoisu.reader.feature.favourite.data.model.toBean
import com.woodnoisu.reader.feature.favourite.data.model.toDomainModel
import com.woodnoisu.reader.feature.favourite.domain.model.FavouriteBookDomainModel
import com.woodnoisu.reader.feature.favourite.domain.repository.FavouriteRepository
import com.woodnoisu.reader.library.base.persistence.BookDao

internal class FavouriteRepositoryImpl(private val bookDao: BookDao): FavouriteRepository {

    override suspend fun getBookList(keyword: String) = if(keyword.isNullOrBlank()){
        //没有关键字搜索，则显示全部
        bookDao.getListByFavorite()
    }else{
        //有关键字搜索，显示搜索内容
        bookDao.getListByName(keyword)
    }.map { it.toDomainModel() }
     .toDomainModel()


    override suspend fun insertBook(modelFavourite: FavouriteBookDomainModel) {
        if (modelFavourite != null) {
            modelFavourite.favorite = 1
            bookDao.insert(modelFavourite.toBean())
        }
    }

    override suspend fun deleteBook(modelFavourite: FavouriteBookDomainModel) {
        if (modelFavourite != null) {
            modelFavourite.favorite = 0
            bookDao.update(modelFavourite.toBean())
        }
    }
}
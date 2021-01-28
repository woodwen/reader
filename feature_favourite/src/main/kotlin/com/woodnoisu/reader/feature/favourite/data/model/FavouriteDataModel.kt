package com.woodnoisu.reader.feature.favourite.data.model

import com.woodnoisu.reader.feature.favourite.domain.model.FavouriteBookDomainModel
import com.woodnoisu.reader.feature.favourite.domain.model.FavouriteDomainModel
import com.woodnoisu.reader.library.base.model.BookBean

internal fun BookBean.toDomainModel(): FavouriteBookDomainModel {
    return FavouriteBookDomainModel(
        id = this.id,
        name = this.name,
        url = this.url,
        category = this.category,
        status = this.status,
        cover = this.cover,
        author = this.author,
        desc = this.desc,
        shopName = this.shopName,
        chaptersUrl = this.chaptersUrl,
        charCount = this.charCount,
        chapterCount = this.chapterCount,
        favorite = this.favorite,
        updateDate = this.updateDate,
        bookFilePath = this.bookFilePath
    )
}

internal fun List<FavouriteBookDomainModel>.toDomainModel() = FavouriteDomainModel(this)

internal fun FavouriteBookDomainModel.toBean(): BookBean {
    return BookBean(
        id = this.id,
        name = this.name,
        url = this.url,
        category = this.category,
        status = this.status,
        cover = this.cover,
        author = this.author,
        desc = this.desc,
        shopName = this.shopName,
        chaptersUrl = this.chaptersUrl,
        charCount = this.charCount,
        chapterCount = this.chapterCount,
        favorite = this.favorite,
        updateDate = this.updateDate,
        bookFilePath = this.bookFilePath,
        chapters = ArrayList()
    )
}

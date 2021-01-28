package com.woodnoisu.reader.feature.album.data.model

import com.woodnoisu.reader.feature.album.domain.model.AlbumBookDomainModel
import com.woodnoisu.reader.library.base.model.BookBean

internal fun BookBean.toDomainModel(): AlbumBookDomainModel {
    return AlbumBookDomainModel(
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

internal fun AlbumBookDomainModel.toBean(): BookBean {
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
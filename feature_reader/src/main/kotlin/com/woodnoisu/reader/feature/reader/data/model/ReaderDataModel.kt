package com.woodnoisu.reader.feature.reader.data.model

import com.woodnoisu.reader.feature.reader.domain.model.ReaderBookDomainModel
import com.woodnoisu.reader.feature.reader.domain.model.ReaderBookSignDomainModel
import com.woodnoisu.reader.feature.reader.domain.model.ReaderChapterDomainModel
import com.woodnoisu.reader.feature.reader.domain.model.ReaderRecordDomainModel
import com.woodnoisu.reader.library.base.model.BookBean
import com.woodnoisu.reader.library.base.model.BookSignBean
import com.woodnoisu.reader.library.base.model.ChapterBean
import com.woodnoisu.reader.library.base.model.ReadRecordBean

internal fun BookBean.toDomainModel(): ReaderBookDomainModel {
    return ReaderBookDomainModel(
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

internal fun ChapterBean.toDomainModel(): ReaderChapterDomainModel {
    return ReaderChapterDomainModel(
            id = this.id,
            shopName = this.shopName,
            bookUrl = this.bookUrl,
            url = this.url,
            name = this.name,
            index = this.index,
            content = this.content,
            start = this.start,
            end = this.end
    )
}

internal fun ReadRecordBean.toDomainModel(): ReaderRecordDomainModel{
    return ReaderRecordDomainModel(
            id = this.id,
            bookUrl = this.bookUrl,
            bookMd5 = this.bookMd5,
            chapterPos = this.chapterPos,
            pagePos = this.pagePos,
            lastRead = this.lastRead
    )
}

internal fun BookSignBean.toDomainModel(): ReaderBookSignDomainModel{
    return ReaderBookSignDomainModel(
            id = this.id,
            bookUrl = this.bookUrl,
            chapterUrl = this.chapterUrl,
            chapterName = this.chapterName,
            saveTime = this.saveTime,
            edit = this.edit
    )
}

internal fun ReaderRecordDomainModel.toBean():ReadRecordBean{
    return ReadRecordBean(
            id = this.id,
            bookUrl = this.bookUrl,
            bookMd5 = this.bookMd5,
            chapterPos = this.chapterPos,
            pagePos = this.pagePos,
            lastRead = this.lastRead
    )
}

internal fun ReaderBookSignDomainModel.toBean(): BookSignBean{
    return BookSignBean(
            id = this.id,
            bookUrl = this.bookUrl,
            chapterUrl = this.chapterUrl,
            chapterName = this.chapterName,
            saveTime = this.saveTime,
            edit = this.edit
    )
}
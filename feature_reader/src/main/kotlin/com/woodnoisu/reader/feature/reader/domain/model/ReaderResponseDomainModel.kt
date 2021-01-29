package com.woodnoisu.reader.feature.reader.domain.model

internal data class ResponseGetBookDomainModel(val book:ReaderBookDomainModel)

internal data class ResponseGetChapterContentsDomainModel(val chapters: List<ReaderChapterDomainModel>)

internal data class ResponseGetChaptersDomainModel(val chapters: List<ReaderChapterDomainModel>,val cacheContents:Boolean)

internal data class ResponseGetBookRecordDomainModel(val record:ReaderRecordDomainModel)

internal data class ResponseGetSignsDomainModel(val signs:List<ReaderBookSignDomainModel>)

internal data class ResponseSetBookRecordDomainModel(val record:ReaderRecordDomainModel)

internal data class ResponseAddSignDomainModel(val sign: ReaderBookSignDomainModel)

internal data class ResponseDeleteSignsDomainModel(val signs:List<ReaderBookSignDomainModel>)
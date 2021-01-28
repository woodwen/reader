package com.woodnoisu.reader.feature.reader.domain.model

internal data class ReaderDomainModel(val readerBookDomainModel:ReaderBookDomainModel? = null,
                                      val readerChapterDomainModels:List<ReaderChapterDomainModel>?=null,
                                      val readerChapterContentDomainModels:List<ReaderChapterDomainModel>?=null,
                                      val cacheContents:Boolean=false,
                                      val readerRecordDomainModel:ReaderRecordDomainModel? = null,
                                      val readerBookSignDomainModels:List<ReaderBookSignDomainModel>?=null,
                                      val chapterContentsErr:Boolean=false)
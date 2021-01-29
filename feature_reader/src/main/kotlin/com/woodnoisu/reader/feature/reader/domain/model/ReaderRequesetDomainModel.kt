package com.woodnoisu.reader.feature.reader.domain.model

internal data class RequestGetBookDomainModel(val bookId:String)

internal data class RequestGetChapterContentsDomainModel(
                                                val chapters: List<ReaderChapterDomainModel>)

internal data class RequestGetChaptersDomainModel(val book:ReaderBookDomainModel,
                                                  val start: Int,
                                                  val limit:Int=100,
                                                  val cacheContents:Boolean=false)

internal data class RequestGetBookRecordDomainModel(val bookUrl:String)

internal data class RequestGetSignsDomainModel(val bookUrl:String)

internal data class RequestSetBookRecordDomainModel(val record:ReaderRecordDomainModel)

internal data class RequestAddSignDomainModel(val bookUrl: String,
                                              val chapterUrl: String,
                                              val chapterName: String)

internal data class RequestDeleteSignsDomainModel(val signs:List<ReaderBookSignDomainModel>)
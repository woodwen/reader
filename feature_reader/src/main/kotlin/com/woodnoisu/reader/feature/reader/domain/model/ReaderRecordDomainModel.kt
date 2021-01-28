package com.woodnoisu.reader.feature.reader.domain.model

internal data class ReaderRecordDomainModel(var id: Int = 0,// 阅读记录id
                                            var bookUrl: String = "",// 书籍地址
                                            var bookMd5: String = "",//所属的书的id
                                            var chapterPos: Int = 0,//阅读到了第几章
                                            var pagePos: Int = 0 ,//当前的页码
                                            var lastRead:String= ""// 上次阅读的时间
                                            )
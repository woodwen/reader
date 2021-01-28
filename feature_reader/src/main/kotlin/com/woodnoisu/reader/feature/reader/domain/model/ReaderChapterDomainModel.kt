package com.woodnoisu.reader.feature.reader.domain.model

internal data class ReaderChapterDomainModel(
                                   var id: Int = 0, // 章节id
                                   var shopName:String="",//书城名字
                                   var bookUrl: String = "",// 书籍地址
                                   var url: String = "", // 章节地址
                                   var name: String = "", // 章节名称
                                   var index: Int = 0,// 章节号
                                   var content: String = "",  // 内容
                                   var start:Long = 0L,  // 开始
                                   var end:Long = 0L// 结束
                                    )
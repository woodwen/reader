package com.woodnoisu.reader.feature.reader.domain.model

internal data class ReaderBookDomainModel(var id: Int = 0,// 书籍id
                                             var name: String = "",// 书名
                                             var url: String = "",// 书籍网络地址
                                             var category: String = "",// 书籍类型
                                             var status: String = "",// 更新状态
                                             var cover: String = "",// 封面地址
                                             var author: String = "",// 作者
                                             var desc: String = "",// 描述
                                             var shopName:String="",//书城名字
                                             var chaptersUrl: String = "",// 目录地址
                                             var charCount: Int = 0,// 字数
                                             var chapterCount: Int = 0,// 章节数
                                             var favorite: Int = 0,// 是否收藏
                                             var updateDate: String = "",// 更新时间
                                             var bookFilePath: String = "",// 书籍文件路径
                                             var chapters: MutableList<ReaderChapterDomainModel> = ArrayList()// 章节列表
                                        )
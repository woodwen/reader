package com.woodnoisu.reader.feature.reader.domain.model

internal data class ReaderBookSignDomainModel(
                                    var id: Int = 0, // 书签id
                                    var bookUrl: String  = "", // 书籍地址
                                    var chapterUrl: String  = "",// 章节地址
                                    var chapterName: String  = "",  // 章节名称
                                    var saveTime: Long = System.currentTimeMillis(), // 保存时间
                                    var edit: Boolean = false // 是否编辑
                                    )
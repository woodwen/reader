package com.woodnoisu.reader.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

/**
 * 书籍
 */
@Entity(tableName = "my_shelf")
@Parcelize
data class BookBean constructor(@PrimaryKey(autoGenerate = true)
                                        var id: Int = 0,// 书籍id
                                        var name: String = "",// 书名
                                        var url: String = "",// 书籍网络地址
                                        var category: String = "",// 书籍类型
                                        var status: String = "",// 更新状态
                                        //var typeUrl: String = "",// 类型地址
                                        var cover: String = "",// 封面地址
                                        var author: String = "",// 作者
                                        var desc: String = "",// 描述
                                        //var source: String = "",// 来源网站
                                        var shopName:String="",//书城名字
                                        var chaptersUrl: String = "",// 目录地址
                                        var charCount: Int = 0,// 字数
                                        var chapterCount: Int = 0,// 章节数
                                        var favorite: Int = 0,// 是否收藏
                                        var updateDate: String = "",// 更新时间
                                        var bookFilePath: String = "",// 书籍文件路径
                                //@Ignore var isLocal: Int = 0,// 是否本地书籍
                                @Ignore var chapters: MutableList<ChapterBean> = ArrayList()// 章节列表(临时使用)
                                ) : Parcelable
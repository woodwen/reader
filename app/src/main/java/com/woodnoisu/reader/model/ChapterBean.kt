package com.woodnoisu.reader.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

/**
 * 章节信息
 */
@Entity(tableName = "cached_chapters", indices = [Index(value = ["url"], unique = true)])
@Parcelize
data class ChapterBean constructor(@PrimaryKey(autoGenerate = true)
                                           var id: Int = 0, // 章节id
                                           var shopName:String="",//书城名字
                                           //var md5: String = "",   // 书籍md5
                                           //var bookName: String = "",// 书名
                                           var bookUrl: String = "",// 书籍地址
                                           var url: String = "", // 章节地址
                                           var name: String = "", // 章节名称
                                           var index: Int = 0,// 章节号
                                           var content: String = "",  // 内容
                                           //var source:String = "",//网站host
                                           var start:Long = 0L,  // 开始
                                           var end:Long = 0L,// 结束
                                    ) : Parcelable
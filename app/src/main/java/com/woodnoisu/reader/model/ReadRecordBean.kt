package com.woodnoisu.reader.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

/**
 * 阅读记录
 */
@Entity(tableName = "my_read_records", indices = [Index(value = ["bookMd5"], unique = true)])
@Parcelize
data class ReadRecordBean constructor(@PrimaryKey(autoGenerate = true)
                                              var id: Int = 0,// 阅读记录id
                                              var bookUrl: String = "",// 书籍地址
                                              var bookMd5: String = "",//所属的书的id
                                              var chapterPos: Int = 0,//阅读到了第几章
                                              var pagePos: Int = 0 ,//当前的页码
                                              var lastRead:String= ""// 上次阅读的时间
                                       ) :Parcelable
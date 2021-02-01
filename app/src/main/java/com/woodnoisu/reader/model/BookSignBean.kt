package com.woodnoisu.reader.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

/**
 * 书签
 */
@Entity(tableName = "my_signs",indices = [Index(value = ["chapterUrl"], unique = true)])
@Parcelize
data class BookSignBean constructor(@PrimaryKey(autoGenerate = true)
                                            var id: Int = 0, // 书签id
                                            var bookUrl: String  = "", // 书籍地址
                                            var chapterUrl: String  = "",// 章节地址
                                            var chapterName: String  = "",  // 章节名称
                                            var saveTime: Long = System.currentTimeMillis(), // 保存时间
                                            var edit: Boolean = false // 是否编辑
                                    ) :Parcelable
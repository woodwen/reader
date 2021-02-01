package com.woodnoisu.reader.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.woodnoisu.reader.model.BookBean
import com.woodnoisu.reader.model.BookSignBean
import com.woodnoisu.reader.model.ChapterBean
import com.woodnoisu.reader.model.ReadRecordBean

/**
 * 数据库操作类
 */
@Database(entities = [
        BookBean::class,
        ChapterBean::class,
        BookSignBean::class,
        ReadRecordBean::class], version = 1, exportSchema = true)
//@TypeConverters(value = [TypeResponseConverter::class])
abstract class AppDataBase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun bookSignDao(): BookSignDao
    abstract fun readRecordDao(): ReadRecordDao

//    companion object {
//
//        @Volatile
//        private var instance: AppDataBase? = null
//
//        fun getDBInstace2(): AppDataBase {
//
//            if (instance == null) {
//                synchronized(AppDataBase::class) {
//                    if (instance == null) {
//                        val MIGRATION_v_v: Migration? = getMigration()
//                        if(MIGRATION_v_v==null){
//                            instance = Room.databaseBuilder(
//                                App.context!!,
//                                AppDataBase::class.java,
//                                "db_novel.db"
//                            ).allowMainThreadQueries()
//                                .build()
//                        }
//                        else{
//                            instance = Room.databaseBuilder(
//                                App.context!!,
//                                AppDataBase::class.java,
//                                "db_novel.db"
//                            ).allowMainThreadQueries()
//                                .addMigrations(MIGRATION_v_v)
//                                .build()
//                        }
//                    }
//                }
//            }
//            return instance!!
//        }
//
//        fun getMigration(): Migration?{
//            return null
//            //第一步，修改版本号，如要添加库的话要在entities里面添加新的表类
//            //第二步，新建需要添加的表的entities和dao,如果不需要新的表，这一步可以省略
//            //第三步，如下：
////                        MIGRATION_v_v = object : Migration(1, 2) {
////                            override fun migrate(database: SupportSQLiteDatabase) {
////                                //为旧表添加新的字段
////                                //database.execSQL("ALTER TABLE User "
////                                //+ " ADD COLUMN book_id TEXT");
////                                //创建新的数据表
////                                database.execSQL("CREATE TABLE IF NOT EXISTS `book` (`book_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT)")
////                            }
////                        }
//            //升级数据库addMigrations(MIGRATION_1_2)
//        }
//    }
}
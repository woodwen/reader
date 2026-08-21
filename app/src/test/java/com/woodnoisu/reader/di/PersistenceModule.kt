package com.woodnoisu.reader.di

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.squareup.moshi.Moshi
import com.woodnoisu.reader.persistence.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {

  @Provides
  @Singleton
  fun provideMoshi(): Moshi {
    return Moshi.Builder().build()
  }

  @Provides
  @Singleton
  fun provideAppDatabase(
    application: Application,
    //typeResponseConverter: TypeResponseConverter
  ): AppDataBase {
    return Room
      .databaseBuilder(application, AppDataBase::class.java, "db_novel.db")
      .addMigrations(MIGRATION_1_2)
      .fallbackToDestructiveMigration()
      //.addTypeConverter(typeResponseConverter)
      .build()
  }

  @Provides
  @Singleton
  fun provideBookDao(appDataBase: AppDataBase): BookDao {
    return appDataBase.bookDao()
  }

  @Provides
  @Singleton
  fun provideChapterDao(appDataBase: AppDataBase): ChapterDao {
    return appDataBase.chapterDao()
  }

  @Provides
  @Singleton
  fun provideBookSignDao(appDataBase: AppDataBase): BookSignDao {
    return appDataBase.bookSignDao()
  }

  @Provides
  @Singleton
  fun provideReadRecordDao(appDataBase: AppDataBase): ReadRecordDao {
    return appDataBase.readRecordDao()
  }

  @Provides
  @Singleton
  fun provideBookSourceDao(appDataBase: AppDataBase): BookSourceDao {
    return appDataBase.bookSourceDao()
  }

//  @Provides
//  @Singleton
//  fun provideTypeResponseConverter(moshi: Moshi): TypeResponseConverter {
//    return TypeResponseConverter(moshi)
//  }

  val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
      database.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `book_sources` (
          `bookSourceName` TEXT NOT NULL,
          `bookSourceGroup` TEXT,
          `bookSourceUrl` TEXT NOT NULL,
          `bookSourceType` INTEGER NOT NULL,
          `bookUrlPattern` TEXT,
          `customOrder` INTEGER NOT NULL,
          `enabled` INTEGER NOT NULL,
          `enabledExplore` INTEGER NOT NULL,
          `header` TEXT,
          `loginUrl` TEXT,
          `bookSourceComment` TEXT,
          `lastUpdateTime` INTEGER NOT NULL,
          `weight` INTEGER NOT NULL,
          `exploreUrl` TEXT,
          `ruleExplore` TEXT,
          `searchUrl` TEXT,
          `ruleSearch` TEXT,
          `ruleBookInfo` TEXT,
          `ruleToc` TEXT,
          `ruleContent` TEXT,
          PRIMARY KEY(`bookSourceUrl`)
        )
        """.trimIndent()
      )
      database.execSQL(
        "CREATE INDEX IF NOT EXISTS `index_book_sources_bookSourceUrl` ON `book_sources` (`bookSourceUrl`)"
      )
    }
  }
}

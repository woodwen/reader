package com.woodnoisu.reader.di

import android.app.Application
import androidx.room.Room
import com.squareup.moshi.Moshi
import com.woodnoisu.ktReader.persistence.*
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

//  @Provides
//  @Singleton
//  fun provideTypeResponseConverter(moshi: Moshi): TypeResponseConverter {
//    return TypeResponseConverter(moshi)
//  }
}

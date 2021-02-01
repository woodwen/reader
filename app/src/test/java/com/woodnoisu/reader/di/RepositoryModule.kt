package com.woodnoisu.reader.di

import com.woodnoisu.ktReader.network.HtmlClient
import com.woodnoisu.ktReader.persistence.BookDao
import com.woodnoisu.ktReader.persistence.BookSignDao
import com.woodnoisu.ktReader.persistence.ChapterDao
import com.woodnoisu.ktReader.persistence.ReadRecordDao
import com.woodnoisu.ktReader.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object RepositoryModule {
    @Provides
    @ActivityRetainedScoped
    fun provideNovelReadRepository(
        htmlClient: HtmlClient,
        bookDao: BookDao,
        bookSignDao: BookSignDao,
        chapterDao: ChapterDao,
        readRecordDao: ReadRecordDao
    ): NovelReadRepository {
        return NovelReadRepository(htmlClient, bookDao, bookSignDao, chapterDao, readRecordDao)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideShelfRepository(
        bookDao: BookDao,
        bookSignDao: BookSignDao,
        chapterDao: ChapterDao,
        readRecordDao: ReadRecordDao
    ): ShelfRepository {
        return ShelfRepository(bookDao, bookSignDao, chapterDao, readRecordDao)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideSquareRepository(
        htmlClient: HtmlClient,
        bookDao: BookDao,
    ): SquareRepository {
        return SquareRepository(htmlClient,bookDao)
    }
}

package com.woodnoisu.reader.di

import com.woodnoisu.reader.network.HtmlClient
import com.woodnoisu.reader.persistence.BookDao
import com.woodnoisu.reader.persistence.BookSignDao
import com.woodnoisu.reader.persistence.ChapterDao
import com.woodnoisu.reader.persistence.ReadRecordDao
import com.woodnoisu.reader.repository.*
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

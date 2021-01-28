package com.woodnoisu.reader.feature.reader.domain.usecase

import com.woodnoisu.reader.feature.reader.domain.model.ReaderChapterDomainModel
import com.woodnoisu.reader.feature.reader.domain.model.ReaderDomainModel
import com.woodnoisu.reader.feature.reader.domain.repository.ReaderRepository
import java.io.IOException

internal class GetChapterContentsUseCase(
        private val readerRepository: ReaderRepository
) {
    sealed class Result {
        data class Success(val data: ReaderDomainModel) : Result()
        data class Error(val e: Throwable) : Result()
    }

    suspend fun execute(chapters: List<ReaderChapterDomainModel>): Result {
        return try {
            Result.Success( readerRepository.getChapterContents(chapters))
        } catch (e: IOException) {
            Result.Error(e)
        }
    }
}
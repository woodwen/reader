package com.woodnoisu.reader.feature.reader.domain.usecase

import com.woodnoisu.reader.feature.reader.domain.model.ReaderDomainModel
import com.woodnoisu.reader.feature.reader.domain.repository.ReaderRepository
import java.io.IOException

internal class GetBookUseCase(
        private val readerRepository: ReaderRepository
) {
    sealed class Result {
        data class Success(val data: ReaderDomainModel) : Result()
        data class Error(val e: Throwable) : Result()
    }

    suspend fun execute(bookId: String): Result {
        return try {
            Result.Success( readerRepository.getBook(bookId))
        } catch (e: IOException) {
            Result.Error(e)
        }
    }
}
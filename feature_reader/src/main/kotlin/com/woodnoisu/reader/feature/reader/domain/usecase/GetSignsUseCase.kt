package com.woodnoisu.reader.feature.reader.domain.usecase

import com.woodnoisu.reader.feature.reader.domain.model.ReaderDomainModel
import com.woodnoisu.reader.feature.reader.domain.repository.ReaderRepository
import java.io.IOException

internal class GetSignsUseCase(
        private val readerRepository: ReaderRepository
) {
    sealed class Result {
        data class Success(val data: ReaderDomainModel) : Result()
        data class Error(val e: Throwable) : Result()
    }

    suspend fun execute(bookUrl: String): Result {
        return try {
            Result.Success(readerRepository.getSigns(bookUrl))
        } catch (e: IOException) {
            Result.Error(e)
        }
    }
}
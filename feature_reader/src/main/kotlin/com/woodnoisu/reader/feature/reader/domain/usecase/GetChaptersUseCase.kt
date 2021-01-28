package com.woodnoisu.reader.feature.reader.domain.usecase

import com.woodnoisu.reader.feature.reader.domain.model.ReaderBookDomainModel
import com.woodnoisu.reader.feature.reader.domain.model.ReaderDomainModel
import com.woodnoisu.reader.feature.reader.domain.repository.ReaderRepository
import java.io.IOException

internal class GetChaptersUseCase(
        private val readerRepository: ReaderRepository
) {
    sealed class Result {
        data class Success(val data: ReaderDomainModel) : Result()
        data class Error(val e: Throwable) : Result()
    }

    suspend fun execute(readerBookDomainModel: ReaderBookDomainModel,
                        start: Int,
                        limit:Int=100,
                        cacheContents:Boolean=false): Result {
        return try {
            Result.Success(readerRepository.getChapters(readerBookDomainModel, start,limit, cacheContents))
        } catch (e: IOException) {
            Result.Error(e)
        }
    }
}
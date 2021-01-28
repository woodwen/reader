package com.woodnoisu.reader.feature.reader.domain.usecase

import com.woodnoisu.reader.feature.reader.domain.model.ReaderBookSignDomainModel
import com.woodnoisu.reader.feature.reader.domain.repository.ReaderRepository
import java.io.IOException

internal class DeleteSignUseCase(
        private val readerRepository: ReaderRepository
) {
    sealed class Result {
        data class Success(val data: String) : Result()
        data class Error(val e: Throwable) : Result()
    }

    suspend fun execute(readerBookSignDomainModels: List<ReaderBookSignDomainModel>): Result {
        return try {
            readerRepository.deleteSigns(readerBookSignDomainModels)
            Result.Success("删除书签成功")
        } catch (e: IOException) {
            Result.Error(e)
        }
    }
}
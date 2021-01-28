package com.woodnoisu.reader.feature.reader.domain.usecase

import com.woodnoisu.reader.feature.reader.domain.model.ReaderRecordDomainModel
import com.woodnoisu.reader.feature.reader.domain.repository.ReaderRepository
import java.io.IOException

internal class SaveBookRecordUseCase(
        private val readerRepository: ReaderRepository
) {
    sealed class Result {
        data class Success(val data: String) : Result()
        data class Error(val e: Throwable) : Result()
    }

    suspend fun execute(readerRecordDomainModel: ReaderRecordDomainModel): Result {
        return try {
            readerRepository.saveBookRecord(readerRecordDomainModel)
            Result.Success("保存阅读记录成功")
        } catch (e: IOException) {
            Result.Error(e)
        }
    }
}
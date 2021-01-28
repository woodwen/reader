package com.woodnoisu.reader.feature.reader.domain.usecase

import com.woodnoisu.reader.feature.reader.domain.repository.ReaderRepository
import java.io.IOException

internal class AddSignUseCase(
        private val readerRepository: ReaderRepository
) {

    sealed class Result {
        data class Success(val data: String) : Result()
        data class Error(val e: Throwable) : Result()
    }

    suspend fun execute(bookUrl: String, chapterUrl: String, chapterName: String): Result {
        return try {
            readerRepository.addSign(bookUrl, chapterUrl, chapterName)
            Result.Success("添加书签成功")
        } catch (e: IOException) {
            Result.Error(e)
        }
    }
}
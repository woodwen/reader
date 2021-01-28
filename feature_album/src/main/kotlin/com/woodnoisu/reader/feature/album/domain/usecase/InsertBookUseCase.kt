package com.woodnoisu.reader.feature.album.domain.usecase

import com.woodnoisu.reader.feature.album.domain.model.AlbumBookDomainModel
import com.woodnoisu.reader.feature.album.domain.repository.AlbumRepository
import java.io.IOException

internal class InsertBookUseCase(
    private val repository: AlbumRepository
) {

    sealed class Result {
        data class Success(val data: String) : Result()
        data class Error(val e: Throwable) : Result()
    }

    suspend fun execute(albumBookDomainModel: AlbumBookDomainModel): Result {
        return try {
            repository.insertBook(albumBookDomainModel)
            Result.Success("新增成功")
        } catch (e: IOException) {
            Result.Error(e)
        }
    }
}

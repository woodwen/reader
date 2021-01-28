package com.woodnoisu.reader.feature.album.domain.usecase

import com.woodnoisu.reader.feature.album.domain.model.AlbumDomainModel
import com.woodnoisu.reader.feature.album.domain.repository.AlbumRepository
import java.io.IOException

internal class GetBookListByTypeUseCase(
    private val repository: AlbumRepository
) {

    sealed class Result {
        data class Success(val data: AlbumDomainModel) : Result()
        data class Error(val e: Throwable) : Result()
    }

    suspend fun execute(shopName:String,typeName: String, page: Int): Result {
        return try {
            Result.Success(repository.getBookListByType(shopName,typeName, page))
        } catch (e: IOException) {
            Result.Error(e)
        }
    }
}

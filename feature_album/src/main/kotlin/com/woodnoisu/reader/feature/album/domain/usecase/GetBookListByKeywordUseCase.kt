package com.woodnoisu.reader.feature.album.domain.usecase

import com.woodnoisu.reader.feature.album.domain.model.AlbumDomainModel
import com.woodnoisu.reader.feature.album.domain.repository.AlbumRepository
import java.io.IOException

internal class GetBookListByKeywordUseCase(
    private val repository: AlbumRepository
) {

    sealed class Result {
        data class Success(val data: AlbumDomainModel) : Result()
        data class Error(val e: Throwable) : Result()
    }

    suspend fun execute(shopName:String,keyword: String, page: Int): Result {
        return try {
            Result.Success(repository.getBookListByKeyword(shopName,keyword, page))
        } catch (e: IOException) {
            Result.Error(e)
        }
    }
}

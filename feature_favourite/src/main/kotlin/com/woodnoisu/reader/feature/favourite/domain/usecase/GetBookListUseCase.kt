package com.woodnoisu.reader.feature.favourite.domain.usecase

import com.woodnoisu.reader.feature.favourite.domain.model.FavouriteDomainModel
import com.woodnoisu.reader.feature.favourite.domain.repository.FavouriteRepository
import java.io.IOException

internal class GetBookListUseCase(
    private val favouriteRepository: FavouriteRepository
) {

    sealed class Result {
        data class Success(val data: FavouriteDomainModel) : Result()
        data class Error(val e: Throwable) : Result()
    }

    suspend fun execute(keyword:String): Result {
        return try {
            Result.Success(favouriteRepository.getBookList(keyword))
        } catch (e: IOException) {
            Result.Error(e)
        }
    }
}

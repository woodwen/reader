package com.woodnoisu.reader.feature.album.domain.usecase

import com.woodnoisu.reader.feature.album.domain.repository.AlbumRepository

internal class GetUseCase(
    private val repository: AlbumRepository
) {
    fun getTypes(shopName:String) = repository.getTypes(shopName)

    fun getParses() = repository.getParses()
}

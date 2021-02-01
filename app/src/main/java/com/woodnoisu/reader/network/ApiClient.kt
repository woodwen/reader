package com.woodnoisu.reader.network

import javax.inject.Inject

class ApiClient @Inject constructor(
    private val apiService: ApiService
) {

//    suspend fun fetchPokemonList(
//        page: Int
//    ) = pokedexService.fetchPokemonList(
//        limit = PAGING_SIZE,
//        offset = page * PAGING_SIZE
//    )
//
//    suspend fun fetchPokemonInfo(
//        name: String
//    ) = pokedexService.fetchPokemonInfo(
//        name = name
//    )

    companion object {
        private const val PAGING_SIZE = 20
    }
}
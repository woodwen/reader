package com.woodnoisu.reader.network

/**
 * 网络服务接口
 */

interface ApiService {

//    /**
//     * 通用异步请求 只需要解析BaseBean
//     */
////    @FormUrlEncoded
//    @POST("wxarticle/chapters")
//    suspend fun request(@FieldMap map: Map<String,ApiService Any>): BaseBean
//
//    /**
//     * 获取公众号列表
//     */
////    @FormUrlEncoded
//    @GET("wxarticle/chapters/json")
//    suspend fun getWXArticle(): ArticleData

//    @GET("pokemon")
//    suspend fun fetchPokemonList(
//        @Query("limit") limit: Int = 20,
//        @Query("offset") offset: Int = 0
//    ): ApiResponse<PokemonResponse>
//
//    @GET("pokemon/{name}")
//    suspend fun fetchPokemonInfo(@Path("name") name: String): ApiResponse<PokemonInfo>
}
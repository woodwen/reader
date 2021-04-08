package com.woodnoisu.reader.di

import com.woodnoisu.ktReader.network.*
import com.woodnoisu.reader.constant.Constant
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

  @Provides
  @Singleton
  fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
      .apply {
        connectTimeout(30, TimeUnit.SECONDS)// 连接时间：30s超时
        readTimeout(10, TimeUnit.SECONDS)// 读取时间：10s超时
        writeTimeout(10, TimeUnit.SECONDS)// 写入时间：10s超时
        addInterceptor(HttpRequestInterceptor())
      }.build()
  }

  @Provides
  @Singleton
  fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
      .client(okHttpClient)
      .baseUrl(Constant.API_BASE_URL)
      .addConverterFactory(MoshiConverterFactory.create())
      //.addCallAdapterFactory(CoroutinesResponseCallAdapterFactory())
      .build()
  }

  @Provides
  @Singleton
  fun provideApiService(retrofit: Retrofit): ApiService {
    return retrofit.create(ApiService::class.java)
  }

  @Provides
  @Singleton
  fun provideApiClient(apiService: ApiService): ApiClient {
    return ApiClient(apiService)
  }

  @Provides
  @Singleton
  fun provideHtmlService(): HtmlService {
    return HtmlService()
  }

  @Provides
  @Singleton
  fun provideHtmlClient(htmlService: HtmlService): HtmlClient {
    return HtmlClient(htmlService)
  }
}

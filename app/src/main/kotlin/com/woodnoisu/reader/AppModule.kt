package com.woodnoisu.reader

import com.facebook.stetho.okhttp3.StethoInterceptor
import com.woodnoisu.reader.app.data.retrofit.AuthenticationInterceptor
import com.woodnoisu.reader.app.data.retrofit.UserAgentInterceptor
import com.woodnoisu.reader.library.base.network.HtmlClient
import com.woodnoisu.reader.library.base.network.HtmlService
import com.woodnoisu.reader.library.base.persistence.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

internal const val MODULE_NAME = "App"

val appModule = Kodein.Module("${MODULE_NAME}Module") {

    bind() from singleton { AuthenticationInterceptor(BuildConfig.GRADLE_API_TOKEN) }

    bind() from singleton { UserAgentInterceptor() }

    bind<HttpLoggingInterceptor>() with singleton {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    bind<Retrofit.Builder>() with singleton { Retrofit.Builder() }

    bind<OkHttpClient.Builder>() with singleton { OkHttpClient.Builder() }

    bind<OkHttpClient>() with singleton {
        instance<OkHttpClient.Builder>()
            .addNetworkInterceptor(StethoInterceptor())
            .addInterceptor(instance<AuthenticationInterceptor>())
            .addInterceptor(instance<UserAgentInterceptor>())
            .addInterceptor(instance<HttpLoggingInterceptor>())
            .build()
    }

    bind<Retrofit>() with singleton {
        instance<Retrofit.Builder>()
            .baseUrl(BuildConfig.GRADLE_API_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(instance())
            .build()
    }

    bind<HtmlService>() with singleton { HtmlService() }

    bind<HtmlClient>() with singleton { HtmlClient(instance()) }

    bind<BookDao>() with singleton { AppDataBase.getDBInstace().bookDao()}

    bind<ChapterDao>() with singleton { AppDataBase.getDBInstace().chapterDao() }

    bind<BookSignDao>() with singleton { AppDataBase.getDBInstace().bookSignDao() }
    
    bind<ReadRecordDao>() with singleton { AppDataBase.getDBInstace().readRecordDao() }
}

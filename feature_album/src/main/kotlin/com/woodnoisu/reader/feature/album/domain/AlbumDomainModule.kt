package com.woodnoisu.reader.feature.album.domain

import com.woodnoisu.reader.feature.album.MODULE_NAME
import com.woodnoisu.reader.feature.album.domain.usecase.GetBookListByKeywordUseCase
import com.woodnoisu.reader.feature.album.domain.usecase.GetBookListByTypeUseCase
import com.woodnoisu.reader.feature.album.domain.usecase.GetBookUseCase
import com.woodnoisu.reader.feature.album.domain.usecase.GetUseCase
import com.woodnoisu.reader.feature.album.domain.usecase.InsertBookUseCase
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

internal val domainModule = Kodein.Module("${MODULE_NAME}DomainModule") {
    bind() from singleton { GetBookListByKeywordUseCase(instance()) }
    bind() from singleton { GetBookListByTypeUseCase(instance()) }
    bind() from singleton { GetBookUseCase(instance()) }
    bind() from singleton { InsertBookUseCase(instance()) }
    bind() from singleton { GetUseCase(instance()) }
}

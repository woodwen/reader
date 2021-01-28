package com.woodnoisu.reader.feature.favourite.domain

import com.woodnoisu.reader.feature.favourite.MODULE_NAME
import com.woodnoisu.reader.feature.favourite.domain.usecase.DeleteBookUseCase
import com.woodnoisu.reader.feature.favourite.domain.usecase.GetBookListUseCase
import com.woodnoisu.reader.feature.favourite.domain.usecase.InsertBookUseCase
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

internal val domainModule = Kodein.Module("${MODULE_NAME}DomainModule") {
    bind() from singleton { GetBookListUseCase(instance()) }
    bind() from singleton { InsertBookUseCase(instance()) }
    bind() from singleton { DeleteBookUseCase(instance()) }
}

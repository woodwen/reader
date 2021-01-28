package com.woodnoisu.reader.feature.reader.domain

import com.woodnoisu.reader.feature.reader.MODULE_NAME
import com.woodnoisu.reader.feature.reader.domain.usecase.*
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

internal val domainModule = Kodein.Module("${MODULE_NAME}DomainModule") {
    bind() from singleton { AddSignUseCase(instance()) }
    bind() from singleton { DeleteSignUseCase(instance()) }
    bind() from singleton { GetBookRecordUseCase(instance()) }
    bind() from singleton { GetBookUseCase(instance()) }
    bind() from singleton { GetChapterContentsUseCase(instance()) }
    bind() from singleton { GetChaptersUseCase(instance()) }
    bind() from singleton { GetSignsUseCase(instance()) }
    bind() from singleton { SaveBookRecordUseCase(instance()) }
}

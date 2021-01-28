package com.woodnoisu.reader.feature.reader.data

import com.woodnoisu.reader.feature.reader.MODULE_NAME
import com.woodnoisu.reader.feature.reader.data.repository.ReaderRepositoryImpl
import com.woodnoisu.reader.feature.reader.domain.repository.ReaderRepository
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

internal val dataModule = Kodein.Module("${MODULE_NAME}DataModule") {
    bind<ReaderRepository>() with singleton { ReaderRepositoryImpl(instance(), instance(),instance(), instance(), instance()) }
}

package com.woodnoisu.reader.feature.favourite.data

import com.woodnoisu.reader.feature.favourite.MODULE_NAME
import com.woodnoisu.reader.feature.favourite.data.repository.FavouriteRepositoryImpl
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

internal val dataModule = Kodein.Module("${MODULE_NAME}DataModule") {
    bind<FavouriteRepositoryImpl>() with singleton { FavouriteRepositoryImpl(instance()) }
}

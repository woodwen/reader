package com.woodnoisu.reader.feature.album.data

import com.woodnoisu.reader.feature.album.MODULE_NAME
import com.woodnoisu.reader.feature.album.data.repository.AlbumRepositoryImpl
import com.woodnoisu.reader.feature.album.domain.repository.AlbumRepository
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton

internal val dataModule = Kodein.Module("${MODULE_NAME}DataModule") {
    bind<AlbumRepository>() with singleton { AlbumRepositoryImpl(instance(), instance()) }
}

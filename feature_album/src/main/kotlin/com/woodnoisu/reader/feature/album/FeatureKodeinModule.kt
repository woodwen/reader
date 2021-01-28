package com.woodnoisu.reader.feature.album

import com.woodnoisu.reader.app.feature.KodeinModuleProvider
import com.woodnoisu.reader.feature.album.data.dataModule
import com.woodnoisu.reader.feature.album.domain.domainModule
import com.woodnoisu.reader.feature.album.presentation.presentationModule
import org.kodein.di.Kodein

internal const val MODULE_NAME = "Album"

object FeatureKodeinModule : KodeinModuleProvider {

    override val kodeinModule = Kodein.Module("${MODULE_NAME}Module") {
        import(presentationModule)
        import(domainModule)
        import(dataModule)
    }
}

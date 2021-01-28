package com.woodnoisu.reader.feature.favourite

import com.woodnoisu.reader.app.feature.KodeinModuleProvider
import com.woodnoisu.reader.feature.favourite.data.dataModule
import com.woodnoisu.reader.feature.favourite.domain.domainModule
import com.woodnoisu.reader.feature.favourite.presentation.presentationModule
import org.kodein.di.Kodein

internal const val MODULE_NAME = "Favourite"

object FeatureKodeinModule : KodeinModuleProvider {

    override val kodeinModule = Kodein.Module("${MODULE_NAME}Module") {
        import(presentationModule)
        import(domainModule)
        import(dataModule)
    }
}

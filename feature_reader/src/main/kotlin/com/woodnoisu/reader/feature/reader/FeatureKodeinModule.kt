package com.woodnoisu.reader.feature.reader

import com.woodnoisu.reader.app.feature.KodeinModuleProvider
import com.woodnoisu.reader.feature.reader.data.dataModule
import com.woodnoisu.reader.feature.reader.domain.domainModule
import com.woodnoisu.reader.feature.reader.presentation.presentationModule
import org.kodein.di.Kodein

internal const val MODULE_NAME = "Reader"

object FeatureKodeinModule : KodeinModuleProvider {

    override val kodeinModule = Kodein.Module("${MODULE_NAME}Module") {
        import(presentationModule)
        import(domainModule)
        import(dataModule)
    }
}

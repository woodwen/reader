package com.woodnoisu.reader.feature.profile

import com.woodnoisu.reader.app.feature.KodeinModuleProvider
import com.woodnoisu.reader.feature.profile.data.dataModule
import com.woodnoisu.reader.feature.profile.domain.domainModule
import com.woodnoisu.reader.feature.profile.presentation.presentationModule
import org.kodein.di.Kodein

internal const val MODULE_NAME = "Profile"

object FeatureKodeinModule : KodeinModuleProvider {

    override val kodeinModule = Kodein.Module("${MODULE_NAME}Module") {
        import(presentationModule)
        import(domainModule)
        import(dataModule)
    }
}

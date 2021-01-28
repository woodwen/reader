package com.woodnoisu.reader.feature.reader.presentation

import androidx.fragment.app.Fragment
import com.woodnoisu.reader.feature.reader.MODULE_NAME
import com.woodnoisu.reader.feature.reader.presentation.reader.ReaderViewModel
import com.woodnoisu.reader.feature.reader.presentation.reader.recyclerView.CatalogueAdapter
import com.woodnoisu.reader.feature.reader.presentation.reader.recyclerView.MarkAdapter
import com.woodnoisu.reader.library.base.di.KotlinViewModelProvider
import org.kodein.di.Kodein
import org.kodein.di.android.x.AndroidLifecycleScope
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.scoped
import org.kodein.di.generic.singleton

internal val presentationModule = Kodein.Module("${MODULE_NAME}PresentationModule") {

    bind<ReaderViewModel>() with scoped<Fragment>(AndroidLifecycleScope).singleton {
        KotlinViewModelProvider.of(context) {
            ReaderViewModel(instance(), instance(), instance(),
                            instance(), instance(), instance(),
                            instance(), instance(), instance(),
                            instance())}}
    bind() from singleton { CatalogueAdapter() }
    bind() from singleton { MarkAdapter() }
}

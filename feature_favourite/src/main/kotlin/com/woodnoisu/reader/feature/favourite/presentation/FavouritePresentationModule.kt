package com.woodnoisu.reader.feature.favourite.presentation

import androidx.fragment.app.Fragment
import com.woodnoisu.reader.feature.favourite.MODULE_NAME
import com.woodnoisu.reader.feature.favourite.presentation.favourite.FavouriteViewModel
import com.woodnoisu.reader.feature.favourite.presentation.favourite.recyclerview.ShelfAdapter
import com.woodnoisu.reader.library.base.di.KotlinViewModelProvider
import org.kodein.di.Kodein
import org.kodein.di.android.x.AndroidLifecycleScope
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.scoped
import org.kodein.di.generic.singleton

internal val presentationModule = Kodein.Module("${MODULE_NAME}PresentationModule") {

    // Favourite
    bind<FavouriteViewModel>() with scoped<Fragment>(AndroidLifecycleScope).singleton {
        KotlinViewModelProvider.of(context) { FavouriteViewModel(instance(),instance(),instance(),instance()) }
    }

    bind() from singleton { ShelfAdapter() }

    //bind() from singleton { ImageLoader(instance()) }
}

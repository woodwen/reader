package com.woodnoisu.reader.feature.album.presentation

import androidx.fragment.app.Fragment
import com.woodnoisu.reader.feature.album.MODULE_NAME
import com.woodnoisu.reader.feature.album.presentation.album.recyclerview.AlbumAdapter
import com.woodnoisu.reader.feature.album.presentation.album.AlbumViewModel
import com.woodnoisu.reader.library.base.di.KotlinViewModelProvider
import org.kodein.di.Kodein
import org.kodein.di.android.x.AndroidLifecycleScope
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.scoped
import org.kodein.di.generic.singleton

internal val presentationModule = Kodein.Module("${MODULE_NAME}PresentationModule") {

    // Album
    bind<AlbumViewModel>() with scoped<Fragment>(AndroidLifecycleScope).singleton {
        KotlinViewModelProvider.of(context) { AlbumViewModel(instance(),instance(), instance(),instance(), instance(), instance()) }
    }

    bind() from singleton { AlbumAdapter() }

    //bind() from singleton { ImageLoader(instance()) }
}

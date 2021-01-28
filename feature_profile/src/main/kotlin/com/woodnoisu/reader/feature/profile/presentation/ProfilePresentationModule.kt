package com.woodnoisu.reader.feature.profile.presentation

import androidx.fragment.app.Fragment
import com.woodnoisu.reader.feature.profile.MODULE_NAME
import com.woodnoisu.reader.feature.profile.presentation.profile.ProfileViewModel
import com.woodnoisu.reader.library.base.di.KotlinViewModelProvider
import org.kodein.di.Kodein
import org.kodein.di.android.x.AndroidLifecycleScope
import org.kodein.di.generic.bind
import org.kodein.di.generic.scoped
import org.kodein.di.generic.singleton

internal val presentationModule = Kodein.Module("${MODULE_NAME}PresentationModule") {
    // Profile
    bind<ProfileViewModel>() with scoped<Fragment>(AndroidLifecycleScope).singleton {
        KotlinViewModelProvider.of(context) { ProfileViewModel() }
    }
}

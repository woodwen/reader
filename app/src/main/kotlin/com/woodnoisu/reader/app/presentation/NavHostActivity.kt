package com.woodnoisu.reader.app.presentation

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.woodnoisu.reader.R
import com.woodnoisu.reader.library.base.presentation.activity.BaseActivity
import com.woodnoisu.reader.library.base.presentation.navigation.NavManager
import kotlinx.android.synthetic.main.activity_nav_host.*
import org.kodein.di.generic.instance

class NavHostActivity : BaseActivity(R.layout.activity_nav_host) {

    private val navController get() = navHostFragment.findNavController()

    private val navManager: NavManager by instance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBottomNavigation()
        initNavManager()
    }

    private fun initBottomNavigation() {
        bottomNav.setupWithNavController(navController)
    }

    private fun initNavManager() {
        navManager.setOnPopBackEvent {
            navController.popBackStack()
        }

        navManager.setOnBottomShowEvent {
            bottomNav.isVisible = it
        }

        navManager.setOnNavEvent {
            navController.navigate(it)
        }
    }
}

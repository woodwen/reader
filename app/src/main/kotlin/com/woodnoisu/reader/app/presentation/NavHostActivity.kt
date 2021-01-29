package com.woodnoisu.reader.app.presentation

import android.os.Bundle
import android.view.KeyEvent
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 在这里，拦截或者监听Android系统的返回键事件。
            // return将拦截。
            // 不做任何处理则默认交由Android系统处理。
        }
        return false
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

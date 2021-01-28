package com.woodnoisu.reader.library.base.presentation.navigation

import androidx.navigation.NavDirections

class NavManager {
    private var navEventListener: ((navDirections: NavDirections) -> Unit)? = null
    private var bottomShowEventListener: ((isBottomShow: Boolean) -> Unit)? = null
    private var popBackEventListener: (() -> Unit)? = null

    fun navigate(navDirections: NavDirections) {
        navEventListener?.invoke(navDirections)
    }

    fun bottomShow(isBottomShow: Boolean){
        bottomShowEventListener?.invoke(isBottomShow)
    }

    fun popBack(){
        popBackEventListener?.invoke()
    }

    fun setOnNavEvent(navEventListener: (navDirections: NavDirections) -> Unit) {
        this.navEventListener = navEventListener
    }

    fun setOnBottomShowEvent(bottomShowEventListener: (isBottomShow: Boolean) -> Unit) {
        this.bottomShowEventListener = bottomShowEventListener
    }

    fun setOnPopBackEvent(popBackEventListener: () -> Unit) {
        this.popBackEventListener = popBackEventListener
    }
}

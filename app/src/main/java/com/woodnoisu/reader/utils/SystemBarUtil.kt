package com.woodnoisu.reader.utils

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowManager

/**
 * Created by newbiechen on 17-5-16.
 * 基于 Android 4.4
 *
 * 主要参数说明:
 *
 * SYSTEM_UI_FLAG_FULLSCREEN : 隐藏StatusBar
 * SYSTEM_UI_FLAG_HIDE_NAVIGATION : 隐藏NavigationBar
 * SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN: 视图扩展到StatusBar的位置，并且StatusBar不消失。
 * 这里需要一些处理，一般是将StatusBar设置为全透明或者半透明。之后还需要使用fitSystemWindows=防止视图扩展到Status
 * Bar上面(会在StatusBar上加一层View，该View可被移动)
 * SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION: 视图扩展到NavigationBar的位置
 * SYSTEM_UI_FLAG_LAYOUT_STABLE:稳定效果
 * SYSTEM_UI_FLAG_IMMERSIVE_STICKY:保证点击任意位置不会退出
 *
 * 可设置特效说明:
 * 1. 全屏特效
 * 2. 全屏点击不退出特效
 * 3. 注意在19 <=sdk <=21 时候，必须通过Window设置透明栏
 */
object SystemBarUtil {

    private const val UNSTABLE_STATUS = View.SYSTEM_UI_FLAG_FULLSCREEN
    private const val UNSTABLE_NAV = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    private const val STABLE_STATUS = View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    private const val STABLE_NAV = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    private const val EXPAND_STATUS =
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    private const val EXPAND_NAV =
        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

    /**
     * 显示系统栏
     */
    fun showSystemBar(activity: Activity,isFullScreen: Boolean) {
        //显示
        SystemBarUtil.showUnStableStatusBar(activity)
        if (isFullScreen) {
            SystemBarUtil.showUnStableNavBar(activity)
        }
    }

    /**
     * 隐藏系统栏
     */
    fun hideSystemBar(activity: Activity,isFullScreen: Boolean) {
        //隐藏
        SystemBarUtil.hideStableStatusBar(activity)
        if (isFullScreen) {
            SystemBarUtil.hideStableNavBar(activity)
        }
    }

    /**
     * 透明StatusBar
     */
    fun transparentStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= 21) {
            expandStatusBar(activity)
            activity.window.statusBarColor =
                activity.resources.getColor(android.R.color.transparent)
        } else if (Build.VERSION.SDK_INT >= 19) {
            val attrs = activity.window.attributes
            attrs.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or attrs.flags
            activity.window.attributes = attrs
        }
    }

    /**
     * 设置隐藏UnStatusBar(点击任意地方会恢复)
     */
    private fun hideUnStableStatusBar(activity: Activity) {
        //App全屏，隐藏StatusBar
        setFlag(activity, UNSTABLE_STATUS)
    }

    /**
     * 显示UnStatusBar
     */
    private fun showUnStableStatusBar(activity: Activity) {
        clearFlag(activity, UNSTABLE_STATUS)
    }

    /**
     * 隐藏StatusBar
     */
    private fun hideStableStatusBar(activity: Activity) {
        //App全屏，隐藏StatusBar
        setFlag(activity, STABLE_STATUS)
    }

    /**
     * 显示StatusBar
     */
    private fun showStableStatusBar(activity: Activity) {
        clearFlag(activity, STABLE_STATUS)
    }

    /**
     * 隐藏UnNavigationBar(点击任意地方会恢复)
     */
    private fun hideUnStableNavBar(activity: Activity) {
        setFlag(activity, UNSTABLE_NAV)
    }

    /**
     * 显示UnNavigationBar
     */
    private fun showUnStableNavBar(activity: Activity) {
        clearFlag(activity, UNSTABLE_NAV)
    }

    /**
     * 隐藏NavigationBar
     */
    private fun hideStableNavBar(activity: Activity) {
        //App全屏，隐藏StatusBar
        setFlag(activity, STABLE_NAV)
    }

    /**
     * 显示NavigationBar
     */
    private fun showStableNavBar(activity: Activity) {
        clearFlag(activity, STABLE_NAV)
    }

    /**
     * 透明NavigationBar
     */
    private  fun transparentNavBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= 21) {
            expandNavBar(activity)
            //下面这个方法在sdk:21以上才有
            activity.window.navigationBarColor =
                activity.resources.getColor(android.R.color.transparent)
        }
    }

    /**
     * 设置ToggleFlag
     */
    private fun setToggleFlag(activity: Activity, option: Int) {
        if (Build.VERSION.SDK_INT >= 19) {
            if (isFlagUsed(activity, option)) {
                clearFlag(activity, option)
            } else {
                setFlag(activity, option)
            }
        }
    }

    /**
     * 视图扩充到StatusBar
     */
    private fun expandStatusBar(activity: Activity) {
        setFlag(activity, EXPAND_STATUS)
    }

    /**
     * 视图扩充到NavBar
     * @param activity
     */
    private fun expandNavBar(activity: Activity) {
        setFlag(activity, EXPAND_NAV)
    }

    /**
     * 设置flag
     */
    private fun setFlag(activity: Activity, flag: Int) {
        if (Build.VERSION.SDK_INT >= 19) {
            val decorView = activity.window.decorView
            val option = decorView.systemUiVisibility or flag
            decorView.systemUiVisibility = option
        }
    }

    /**
     * 取消flag
     */
    private fun clearFlag(activity: Activity, flag: Int) {
        if (Build.VERSION.SDK_INT >= 19) {
            val decorView = activity.window.decorView
            val option = decorView.systemUiVisibility and flag.inv()
            decorView.systemUiVisibility = option
        }
    }

    /**
     * @param activity
     * @return flag是否已被使用
     */
    private fun isFlagUsed(activity: Activity, flag: Int): Boolean {
        val currentFlag = activity.window.decorView.systemUiVisibility
        return currentFlag and flag == flag
    }
}

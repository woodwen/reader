package com.woodnoisu.reader.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager

import androidx.appcompat.app.AppCompatActivity

/**
 * 状态栏帮助类
 */
object StatusBarUtil {

    /**
     * 设置Activity对应的顶部状态栏的颜色
     */
    fun setWindowStatusBarColor(activity: Activity, colorResId: Int) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window: Window = activity.window
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.setStatusBarColor(activity.resources.getColor(colorResId))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 设置Dialog对应的顶部状态栏的颜色
     */
    fun setWindowStatusBarColor(dialog: Dialog, colorResId: Int) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val window: Window = dialog.getWindow()!!
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.setStatusBarColor(dialog.getContext().getResources().getColor(colorResId))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    /**
     * 获取信号栏高度
     * @return
     */
    fun getStateBarHeigh(context: Context): Int
    {
        var statusBarHeight1 = 0
        //获取status_bar_height资源的ID
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0)
        {
            //根据资源ID获取响应的尺寸值
            statusBarHeight1 = context.resources.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight1
    }

    /**
     * 设置状态栏风格
     */
    fun setBarsStyle(activity: AppCompatActivity, color: Int, dark: Boolean) {
        setStatusBarLightModeMIUI(activity, dark)
        setStatusBarLightModeFlyme(activity, dark)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0及以上
            val decorView = activity.window.decorView
            if (dark) {
                val option = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                decorView.systemUiVisibility = option
                activity.window.statusBarColor = activity.resources.getColor(color)
            } else {
                val option = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                decorView.systemUiVisibility = option
                activity.window.statusBarColor = activity.resources.getColor(color)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4到5.0
            val localLayoutParams = activity.window.attributes
            localLayoutParams.flags =
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or localLayoutParams.flags
        }

        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//android6.0以后可以对状态栏文字颜色和图标进行修改
        //            if (dark){
        //                activity.getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        //            }else {
        //                activity.getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        //            }
        //
        //        }
    }

    /**
     * MIUI风格
     */
    private fun setStatusBarLightModeMIUI(activity: AppCompatActivity, dark: Boolean): Boolean {
        var result = false
        if (activity.window != null) {
            val clazz = activity.window.javaClass
            try {
                var darkModeFlag = 0
                val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
                val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
                darkModeFlag = field.getInt(layoutParams)
                val extraFlagField = clazz.getMethod(
                    "setExtraFlags",
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType
                )
                if (dark) {
                    extraFlagField.invoke(activity.window, darkModeFlag, darkModeFlag)//状态栏透明且黑色字体
                } else {
                    extraFlagField.invoke(activity.window, 0, darkModeFlag)//清除黑色字体
                }
                result = true
            } catch (e: Exception) {

            }

        }
        return result
    }

    /**
     * Flyme风格
     */
    private fun setStatusBarLightModeFlyme(activity: AppCompatActivity, dark: Boolean): Boolean {
        var result = false
        if (activity.window != null) {
            try {
                val lp = activity.window.attributes
                val darkFlag = WindowManager.LayoutParams::class.java
                    .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
                val meizuFlags = WindowManager.LayoutParams::class.java
                    .getDeclaredField("meizuFlags")
                darkFlag.isAccessible = true
                meizuFlags.isAccessible = true
                val bit = darkFlag.getInt(null)
                var value = meizuFlags.getInt(lp)
                if (dark) {
                    value = value or bit
                } else {
                    value = value and bit.inv()
                }
                meizuFlags.setInt(lp, value)
                activity.window.attributes = lp
                result = true
            } catch (e: Exception) {

            }

        }
        return result
    }
}

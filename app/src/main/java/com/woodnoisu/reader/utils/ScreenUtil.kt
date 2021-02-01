package com.woodnoisu.reader.utils

import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity

/**
 * 屏幕帮助类
 */
object ScreenUtil {
    /**
     * dp转px
     */
    fun dpToPx(dp: Int): Int {
        val metrics = getDisplayMetrics()
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), metrics).toInt()
    }

    /**
     * px转dp
     */
    fun pxToDp(px: Int): Int {
        val metrics = getDisplayMetrics()
        return (px / metrics.density).toInt()
    }

    /**
     * sp转px
     */
    fun spToPx(sp: Int): Int {
        val metrics = getDisplayMetrics()
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), metrics).toInt()
    }

    /**
     * px转sp
     */
    fun pxToSp(px: Int): Int {
        val metrics = getDisplayMetrics()
        return (px / metrics.scaledDensity).toInt()
    }

    /**
     * 获取手机显示App区域的大小（头部导航栏+ActionBar+根布局），不包括虚拟按钮
     * @return
     */
    fun getAppSize(): IntArray{
        val size = IntArray(2)
        val metrics = getDisplayMetrics()
        size[0] = metrics.widthPixels
        size[1] = metrics.heightPixels
        return size
    }

    /**
     * 获取导航栏的高度
     * @return
     */
    fun getStatusBarHeight(): Int{
        val resources = Resources.getSystem()
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return resources.getDimensionPixelSize(resourceId)
    }

    /**
     * 获取虚拟按键的高度
     * @return
     */
    fun getNavigationBarHeight(): Int{
        var navigationBarHeight = 0
        val rs = Resources.getSystem()
        val id = rs.getIdentifier("navigation_bar_height", "dimen", "android")
        if (id > 0 && hasNavigationBar()) {
            navigationBarHeight = rs.getDimensionPixelSize(id)
        }
        return navigationBarHeight
    }

    /**
     * 获取整个手机屏幕的大小(包括虚拟按钮)
     * 必须在onWindowFocus方法之后使用
     * @param activity
     * @return
     */
    fun getScreenSize(activity: AppCompatActivity): IntArray {
        val size = IntArray(2)
        val decorView = activity.window.decorView
        size[0] = decorView.width
        size[1] = decorView.height
        return size
    }

    /**
     * 显示指标
     */
    private fun getDisplayMetrics(): DisplayMetrics {
        return Resources.getSystem().displayMetrics
    }

    /**
     * 是否存在虚拟按键
     * @return
     */
    private fun hasNavigationBar(): Boolean {
        var hasNavigationBar = false
        val rs = Resources.getSystem()
        val id = rs.getIdentifier("config_showNavigationBar", "bool", "android")
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id)
        }
        try {
            val systemPropertiesClass = Class.forName("android.os.SystemProperties")
            val m = systemPropertiesClass.getMethod("get", String::class.java)
            val navBarOverride = m.invoke(systemPropertiesClass, "qemu.hw.mainkeys") as String
            if ("1" == navBarOverride) {
                hasNavigationBar = false
            } else if ("0" == navBarOverride) {
                hasNavigationBar = true
            }
        } catch (e: Exception) {
        }

        return hasNavigationBar
    }
}

package com.woodnoisu.reader.library.base.utils

import android.app.Activity
import android.os.Build
import android.view.WindowManager

object SystemUtil {
    fun screenAlwaysOn(activity: Activity) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    fun layoutInDisplayCutoutMode(activity: Activity) {
        // 自定义显示模式
        val lp = activity.window.attributes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lp.layoutInDisplayCutoutMode = 1
        }
        activity.window.attributes = lp
    }
}
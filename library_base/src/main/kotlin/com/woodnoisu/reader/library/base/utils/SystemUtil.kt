package com.woodnoisu.reader.library.base.utils

import android.app.Activity
import android.view.WindowManager

object SystemUtil {
    fun screenAlwaysOn(activity: Activity) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
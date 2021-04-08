package com.woodnoisu.reader

import android.app.Application
import com.woodnoisu.reader.constant.Constant
import dagger.hilt.android.HiltAndroidApp
import java.io.File

/**
 * app对象
 */

@HiltAndroidApp
class App() : Application(){
    /**
     * 创建事件
     */
    override fun onCreate() {
        super.onCreate()

        // 初始化文件夹
        if (!File(Constant.BOOK_CACHE_PATH).exists()) {
            File(Constant.BOOK_CACHE_PATH).mkdir()
        }
    }
}

package com.woodnoisu.reader.library.base.utils

import android.widget.Toast

/**
 * 显示通知栏
 */
fun showToast(text: String) {
    if(!text.isNullOrBlank()){
        Toast.makeText(ContextProvider.mContext, text, Toast.LENGTH_SHORT).show()
    }
}
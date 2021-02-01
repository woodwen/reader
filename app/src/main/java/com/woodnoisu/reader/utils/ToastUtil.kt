package com.woodnoisu.reader.utils

import android.widget.Toast
import com.woodnoisu.reader.base.ContextProvider

/**
 * 显示通知栏
 */
fun showToast(text: String) {
    Toast.makeText(ContextProvider.mContext, text, Toast.LENGTH_SHORT).show()
}
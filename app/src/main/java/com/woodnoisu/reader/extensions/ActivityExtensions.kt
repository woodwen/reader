package com.woodnoisu.reader.extensions

import android.os.Parcelable
import androidx.activity.ComponentActivity

/**
 * 延迟初始化一个可拆分参数。
 */
fun <T : Parcelable> ComponentActivity.argument(key: String): Lazy<T> {
  return lazy { requireNotNull(intent.getParcelableExtra<T>(key)) }
}

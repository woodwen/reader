package com.woodnoisu.reader.extensions

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.woodnoisu.reader.utils.ExceptionUtil
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Int转px
 */
fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

/**
 * ViewModel扩展方法：启动协程
 * @param block 协程逻辑
 * @param onError 错误回调方法
 * @param onComplete 完成回调方法
 */
fun ViewModel.launch(
    block: suspend CoroutineScope.() -> Unit,
    onError: (e: Throwable) -> Unit = {},
    onComplete: () -> Unit = {}
) {
    viewModelScope.launch(
        CoroutineExceptionHandler { _, throwable ->
            run {
                // 这里统一处理错误
                ExceptionUtil.catchException(
                    throwable
                )
                onError(throwable)
            }
        }
    ) {
        try {
            block.invoke(this)
        } finally {
            onComplete()
        }
    }
}

enum class Position
{
    LEFT, RIGHT, TOP, BOTTOM
}

/**
 * String Null 转 ""
 */
fun String?.toNotNull():String = if(!this.isNullOrBlank()) this else ""
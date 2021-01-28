package com.woodnoisu.reader.feature.reader.presentation.page.event

/**
 * 触摸接口
 */
internal interface OnTouchListener {
    fun onTouch(): Boolean
    fun center()
    fun prePage()
    fun nextPage()
    fun cancel()
}
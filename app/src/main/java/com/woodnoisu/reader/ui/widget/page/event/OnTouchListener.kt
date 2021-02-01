package com.woodnoisu.reader.ui.widget.page.event

/**
 * 触摸接口
 */
interface OnTouchListener {
    fun onTouch(): Boolean
    fun center()
    fun prePage()
    fun nextPage()
    fun cancel()
}
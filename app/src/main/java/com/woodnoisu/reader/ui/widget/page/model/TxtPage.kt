package com.woodnoisu.reader.ui.widget.page.model

/**
 * 显示页面
 */
class TxtPage {
    var position = 0
    var title: String = ""
    var titleLines = 0 //当前 lines 中为 title 的行数。
    val lines: MutableList<String> = ArrayList()
    var pic: String = ""
}
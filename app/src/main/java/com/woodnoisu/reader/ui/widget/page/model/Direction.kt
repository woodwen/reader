package com.woodnoisu.reader.ui.widget.page.model

enum class Direction(val isHorizontal: Boolean) {
    NONE(true),
    NEXT(true),
    PRE(true),
    UP(false),
    DOWN(false);
}
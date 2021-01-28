package com.woodnoisu.reader.feature.reader.presentation.page.model

enum class Direction(val isHorizontal: Boolean) {
    NONE(true),
    NEXT(true),
    PRE(true),
    UP(false),
    DOWN(false);
}
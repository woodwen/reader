package com.woodnoisu.reader.feature.reader.presentation.page.event

internal interface OnCurPageChangeListener {
    fun hasPrev(): Boolean
    fun hasNext(): Boolean
    fun pageCancel()
}
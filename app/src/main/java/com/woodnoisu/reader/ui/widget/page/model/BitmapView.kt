package com.woodnoisu.reader.ui.widget.page.model

import android.graphics.Bitmap
import android.graphics.Rect

/**
 * 图片view
 */
class BitmapView {
    internal var bitmap: Bitmap? = null
    internal var srcRect: Rect? = null
    internal var destRect: Rect? = null
    internal var top: Int = 0
    internal var bottom: Int = 0
}
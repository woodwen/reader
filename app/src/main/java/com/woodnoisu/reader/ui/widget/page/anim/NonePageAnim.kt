package com.woodnoisu.reader.ui.widget.page.anim

import android.graphics.Canvas
import android.view.View
import com.woodnoisu.reader.ui.widget.page.event.OnCurPageChangeListener

/**
 * 空动画
 */
class NonePageAnim(w: Int, h: Int, view: View, listenerCur: OnCurPageChangeListener) :
    HorizonPageAnim(w, h, view, listenerCur) {

    /**
     * 绘制静态
     */
    override fun drawStatic(canvas: Canvas) {
        if (isCancel) {
            canvas.drawBitmap(mCurBitmap, 0f, 0f, null)
        } else {
            canvas.drawBitmap(mNextBitmap, 0f, 0f, null)
        }
    }

    /**
     * 绘制移动
     */
    override fun drawMove(canvas: Canvas) {
        if (isCancel) {
            canvas.drawBitmap(mCurBitmap, 0f, 0f, null)
        } else {
            canvas.drawBitmap(mNextBitmap, 0f, 0f, null)
        }
    }

    /**
     * 开始动画
     */
    override fun startAnimExt() {}
}

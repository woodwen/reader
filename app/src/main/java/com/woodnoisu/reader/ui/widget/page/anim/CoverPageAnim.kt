package com.woodnoisu.reader.ui.widget.page.anim

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.view.View
import com.woodnoisu.reader.ui.widget.page.event.OnCurPageChangeListener
import com.woodnoisu.reader.ui.widget.page.model.Direction

/**
 * 覆盖模式动画
 */
class CoverPageAnim(w: Int, h: Int, view: View, listenerCur: OnCurPageChangeListener) :
    HorizonPageAnim(w, h, view, listenerCur) {

    // 原始尺寸
    private val mSrcRect: Rect = Rect(0, 0, mViewWidth, mViewHeight)
    // 目标尺寸
    private val mDestRect: Rect = Rect(0, 0, mViewWidth, mViewHeight)
    //渐变工具
    private val mBackShadowDrawableLR: GradientDrawable

    /**
     * 初始化
     */
    init {
        val mBackShadowColors = intArrayOf(0x66000000, 0x00000000)
        mBackShadowDrawableLR = GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT, mBackShadowColors)
        mBackShadowDrawableLR.gradientType = GradientDrawable.LINEAR_GRADIENT
    }

    /**
     * 绘制静态
     */
    override fun drawStatic(canvas: Canvas) {
        if (isCancel) {
            mNextBitmap = mCurBitmap.copy(Bitmap.Config.RGB_565, true)
            canvas.drawBitmap(mCurBitmap, 0f, 0f, null)
        } else {
            canvas.drawBitmap(mNextBitmap, 0f, 0f, null)
        }
    }

    /**
     * 绘制移动
     */
    override fun drawMove(canvas: Canvas) {

        when (mDirection) {
            Direction.NEXT -> {
                var dis = (mViewWidth - mStartX + mTouchX).toInt()
                if (dis > mViewWidth) {
                    dis = mViewWidth
                }
                //计算bitmap截取的区域
                mSrcRect.left = mViewWidth - dis
                //计算bitmap在canvas显示的区域
                mDestRect.right = dis
                canvas.drawBitmap(mNextBitmap, 0f, 0f, null)
                canvas.drawBitmap(mCurBitmap, mSrcRect, mDestRect, null)
                addShadow(dis, canvas)
            }
            else -> {
                mSrcRect.left = (mViewWidth - mTouchX).toInt()
                mDestRect.right = mTouchX.toInt()
                canvas.drawBitmap(mCurBitmap, 0f, 0f, null)
                canvas.drawBitmap(mNextBitmap, mSrcRect, mDestRect, null)
                addShadow(mTouchX.toInt(), canvas)
            }
        }
    }

    /**
     * 开始动画
     */
    override fun startAnimExt() {
        //super.startAnim()
        var dx: Int
        when (mDirection) {
            Direction.NEXT -> if (isCancel) {
                var dis = (mViewWidth - mStartX + mTouchX).toInt()
                if (dis > mViewWidth) {
                    dis = mViewWidth
                }
                dx = mViewWidth - dis
            } else {
                dx = (-(mTouchX + (mViewWidth - mStartX))).toInt()
            }
            else -> if (isCancel) {
                dx = (-mTouchX).toInt()
            } else {
                dx = (mViewWidth - mTouchX).toInt()
            }
        }

        //滑动速度保持一致
        val duration = 400 * Math.abs(dx) / mViewWidth
        mScroller.startScroll(mTouchX.toInt(), 0, dx, 0, duration)
    }

    /**
     * 添加阴影
     */
    private fun addShadow(left: Int, canvas: Canvas) {
        mBackShadowDrawableLR.setBounds(left, 0, left + 30, mScreenHeight)
        mBackShadowDrawableLR.draw(canvas)
    }
}

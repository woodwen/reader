package com.woodnoisu.reader.ui.widget.page.anim

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import com.woodnoisu.reader.ui.widget.page.event.OnCurPageChangeListener
import com.woodnoisu.reader.ui.widget.page.model.Direction
import kotlin.math.abs

/**
 * 横滑动画
 */
class SlidePageAnim(w: Int, h: Int, view: View, listenerCur: OnCurPageChangeListener) :
    HorizonPageAnim(w, h, view, listenerCur) {

    // 当前原始区域
    private val mSrcRect: Rect = Rect(0, 0, mViewWidth, mViewHeight)
    // 当前目标区域
    private val mDestRect: Rect = Rect(0, 0, mViewWidth, mViewHeight)
    // 下一页原始区域
    private val mNextSrcRect: Rect = Rect(0, 0, mViewWidth, mViewHeight)
    // 下一页目标区域
    private val mNextDestRect: Rect = Rect(0, 0, mViewWidth, mViewHeight)

    /**
     * 开始动画
     */
    override fun startAnimExt() {
        //super.startAnim()
        val dx: Int
        when (mDirection) {
            Direction.NEXT -> {
                if (isCancel) {
                    var dis = (mScreenWidth - mStartX + mTouchX).toInt()
                    if (dis > mScreenWidth) {
                        dis = mScreenWidth
                    }
                    dx = mScreenWidth - dis
                } else {
                    dx = (-(mTouchX + (mScreenWidth - mStartX))).toInt()
                }
            }
            else -> {
                dx = if (isCancel) {
                    (-abs(mTouchX - mStartX)).toInt()
                } else {
                    (mScreenWidth - (mTouchX - mStartX)).toInt()
                }
            }
        }
        //滑动速度保持一致
        val duration = 400 * abs(dx) / mScreenWidth
        mScroller.startScroll(mTouchX.toInt(), 0, dx, 0, duration)
    }

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
        var dis: Int
        when (mDirection) {
            Direction.NEXT -> {
                //左半边的剩余区域
                dis = (mScreenWidth - mStartX + mTouchX).toInt()
                if (dis > mScreenWidth) {
                    dis = mScreenWidth
                }
                //计算bitmap截取的区域
                mSrcRect.left = mScreenWidth - dis
                //计算bitmap在canvas显示的区域
                mDestRect.right = dis
                //计算下一页截取的区域
                mNextSrcRect.right = mScreenWidth - dis
                //计算下一页在canvas显示的区域
                mNextDestRect.left = dis

                canvas.drawBitmap(mNextBitmap, mNextSrcRect, mNextDestRect, null)
                canvas.drawBitmap(mCurBitmap, mSrcRect, mDestRect, null)
            }
            else -> {
                dis = (mTouchX - mStartX).toInt()
                if (dis < 0) {
                    dis = 0
                    mStartX = mTouchX
                }
                mSrcRect.left = mScreenWidth - dis
                mDestRect.right = dis

                //计算下一页截取的区域
                mNextSrcRect.right = mScreenWidth - dis
                //计算下一页在canvas显示的区域
                mNextDestRect.left = dis

                canvas.drawBitmap(mCurBitmap, mNextSrcRect, mNextDestRect, null)
                canvas.drawBitmap(mNextBitmap, mSrcRect, mDestRect, null)
            }
        }
    }
}

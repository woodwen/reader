package com.woodnoisu.reader.ui.widget.page

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Scroller
import com.woodnoisu.reader.ui.widget.page.event.OnCurPageChangeListener
import com.woodnoisu.reader.ui.widget.page.model.Direction

abstract class PageAnimation(w:Int, h:Int, marginWidth:Int, marginHeight:Int,
                             view:View, listenerCur: OnCurPageChangeListener) {
    //正在使用的View
    protected var mView: View

    //滑动装置
    protected var mScroller: Scroller

    //监听器
    protected var mListenerCur: OnCurPageChangeListener

    //移动方向
    protected var mDirection: Direction = Direction.NONE

    /**
     * 是否在运行
     */
    var isRunning = false
        protected set

    //屏幕的尺寸
    protected var mScreenWidth = 0
    protected var mScreenHeight = 0

    //屏幕的间距
    protected var mMarginWidth = 0
    protected var mMarginHeight = 0

    //视图的尺寸
    protected var mViewWidth = 0
    protected var mViewHeight = 0

    //起始点
    protected var mStartX = 0f
    protected var mStartY = 0f

    //触碰点
    protected var mTouchX = 0f
    protected var mTouchY = 0f

    //上一个触碰点
    protected var mLastX = 0f
    protected var mLastY = 0f

    /**
     * 初始化
     */
    init {
        mScreenWidth = w
        mScreenHeight = h

        mMarginWidth = marginWidth
        mMarginHeight = marginHeight

        mViewWidth = mScreenWidth - mMarginWidth * 2
        mViewHeight = mScreenHeight - mMarginHeight * 2

        mView = view

        mListenerCur = listenerCur

        mScroller = Scroller(mView.context, LinearInterpolator())
    }

    /**
     * 设置开始点
     */
    fun setStartPoint(x: Float, y: Float) {
        mStartX = x
        mStartY = y

        mLastX = mStartX
        mLastY = mStartY

        setStartPointExt(x, y)
    }

    /**
     * 设置接触点
     */
    fun setTouchPoint(x: Float, y: Float) {
        mLastX = mTouchX
        mLastY = mTouchY

        mTouchX = x
        mTouchY = y

        setTouchPointExt(x, y)
    }

    /**
     * 开启翻页动画
     */
    fun startAnim() {
        if(startAnimBefore()){
            if (isRunning) {
                startAnimExt()
                return
            }
            isRunning = true
            startAnimExt()
        }
    }

    /**
     * 设置方向
     */
    fun setDirection(direction: Direction) {
        mDirection = direction
        setDirectionExt(mDirection)
    }

    /**
     * 清理
     */
    fun clear() {
        //mView = null
    }

    /**
     * 设置开始点扩展
     */
    open fun setStartPointExt(x: Float, y: Float){}

    /**
     * 设置接触点扩展
     */
    open fun setTouchPointExt(x: Float, y: Float){}

    /**
     * 开启翻页动画前
     */
    open fun startAnimBefore():Boolean {return true}

    /**
     * 开启翻页动画扩展
     */
    open fun startAnimExt(){}

    /**
     * 设置方向(扩展)
     */
    open fun setDirectionExt(direction: Direction){}

    /**
     * 点击事件的处理
     * @param event
     */
    abstract fun onTouchEvent(event: MotionEvent): Boolean

    /**
     * 绘制图形
     * @param canvas
     */
    abstract fun draw(canvas: Canvas)

    /**
     * 滚动动画
     * 必须放在computeScroll()方法中执行
     */
    abstract fun scrollAnim()

    /**
     * 取消动画
     */
    abstract fun abortAnim()

    /**
     * 获取背景板
     * @return
     */
    abstract fun getBgBitmap(): Bitmap?

    /**
     * 获取内容显示版面
     */
    abstract fun getNextBitmap(): Bitmap?
}
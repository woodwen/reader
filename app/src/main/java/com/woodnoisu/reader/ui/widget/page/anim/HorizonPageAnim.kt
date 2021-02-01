package com.woodnoisu.reader.ui.widget.page.anim

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.woodnoisu.reader.ui.widget.page.PageAnimation
import com.woodnoisu.reader.ui.widget.page.event.OnCurPageChangeListener
import com.woodnoisu.reader.ui.widget.page.model.Direction
import kotlin.math.abs

/**
 * 横向动画
 */
abstract class HorizonPageAnim(
    w: Int, h: Int, marginWidth: Int, marginHeight: Int,
    view: View, listenerCur: OnCurPageChangeListener
) : PageAnimation(w, h, marginWidth, marginHeight, view, listenerCur) {

    // 当前图片
    protected var mCurBitmap: Bitmap
    // 下一张图片
    protected var mNextBitmap: Bitmap
    // 是否取消翻页
    protected var isCancel = false
    // 移动x
    private var mMoveX = 0
    // 移动y
    private var mMoveY = 0
    // 是否移动了
    private var isMove = false
    //是否翻阅下一页。true表示翻到下一页，false表示上一页。
    private var isNext = false
    //是否没下一页或者上一页
    private var noNext = false

    /**
     * 构造函数
     */
    constructor(w: Int, h: Int, view: View, listenerCur: OnCurPageChangeListener) : this(
        w,
        h,
        0,
        0,
        view,
        listenerCur
    )

    /**
     * 初始化
     */
    init {
        //创建图片
        mCurBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.RGB_565)
        mNextBitmap = Bitmap.createBitmap(mViewWidth, mViewHeight, Bitmap.Config.RGB_565)
    }

    /**
     * 设置触摸事件
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        //获取点击位置
        val x = event.x.toInt()
        val y = event.y.toInt()
        //设置触摸点
        setTouchPoint(x.toFloat(), y.toFloat())

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                //移动的点击位置
                mMoveX = 0
                mMoveY = 0
                //是否移动
                isMove = false
                //是否存在下一章
                noNext = false
                //是下一章还是前一章
                isNext = false
                //是否正在执行动画
                isRunning = false
                //取消
                isCancel = false
                //设置起始位置的触摸点
                setStartPoint(x.toFloat(), y.toFloat())
                //如果存在动画则取消动画
                abortAnim()
            }
            MotionEvent.ACTION_MOVE -> {
                val slop = ViewConfiguration.get(mView.context).scaledTouchSlop
                //判断是否移动了
                if (!isMove) {
                    isMove = abs(mStartX - x) > slop || abs(mStartY - y) > slop
                }

                if (isMove) {
                    //判断是否是准备移动的状态(将要移动但是还没有移动)
                    if (mMoveX == 0 && mMoveY == 0) {
                        //判断翻得是上一页还是下一页
                        if (x - mStartX > 0) {
                            //上一页的参数配置
                            isNext = false
                            val hasPrev = mListenerCur.hasPrev()
                            setDirection(Direction.PRE)
                            //如果上一页不存在
                            if (!hasPrev) {
                                noNext = true
                                return true
                            }
                        } else {
                            //进行下一页的配置
                            isNext = true
                            //判断是否下一页存在
                            val hasNext = mListenerCur.hasNext()
                            //如果存在设置动画方向
                            setDirection(Direction.NEXT)

                            //如果不存在表示没有下一页了
                            if (!hasNext) {
                                noNext = true
                                return true
                            }
                        }
                    } else {
                        //判断是否取消翻页
                        isCancel = if (isNext) {
                            x - mMoveX > 0
                        } else {
                            x - mMoveX < 0
                        }
                    }

                    mMoveX = x
                    mMoveY = y
                    isRunning = true
                    mView.invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!isMove) {
                    isNext = x >= mScreenWidth / 2

                    if (isNext) {
                        //判断是否下一页存在
                        val hasNext = mListenerCur.hasNext()
                        //设置动画方向
                        setDirection(Direction.NEXT)
                        if (!hasNext) {
                            return true
                        }
                    } else {
                        val hasPrev = mListenerCur.hasPrev()
                        setDirection(Direction.PRE)
                        if (!hasPrev) {
                            return true
                        }
                    }
                }

                // 是否取消翻页
                if (isCancel) {
                    mListenerCur.pageCancel()
                }

                // 开启翻页效果
                if (!noNext) {
                    startAnim()
                    mView.invalidate()
                }
            }
        }
        return true
    }

    /**
     * 滚动动画
     */
    override fun scrollAnim() {
        if (mScroller.computeScrollOffset()) {
            val x = mScroller.currX
            val y = mScroller.currY

            setTouchPoint(x.toFloat(), y.toFloat())

            if (mScroller.finalX == x && mScroller.finalY == y) {
                isRunning = false
            }
            mView.postInvalidate()
        }
    }

    /**
     * 取消动画
     */
    override fun abortAnim() {
        if (!mScroller.isFinished) {
            mScroller.abortAnimation()
            isRunning = false
            setTouchPoint(mScroller.finalX.toFloat(), mScroller.finalY.toFloat())
            mView.postInvalidate()
        }
    }

    /**
     * 获取背景图片
     */
    override fun getBgBitmap(): Bitmap {
        return mNextBitmap
    }

    /**
     * 获取下一章图片
     */
    override fun getNextBitmap(): Bitmap {
        return mNextBitmap
    }

    /**
     * 绘图
     */
    override fun draw(canvas: Canvas) {
        if (isRunning) {
            drawMove(canvas)
        } else {
            if (isCancel) {
                mNextBitmap = mCurBitmap.copy(Bitmap.Config.RGB_565, true)
            }
            drawStatic(canvas)
        }
    }

    /**
     * 绘制静态
     */
    abstract fun drawStatic(canvas: Canvas)

    /**
     * 绘制移动
     */
    abstract fun drawMove(canvas: Canvas)

    /**
     * 转换页面，在显示下一章的时候，必须首先调用此方法
     */
    fun changePage() {
        val bitmap = mCurBitmap
        mCurBitmap = mNextBitmap
        mNextBitmap = bitmap
    }
}

package com.woodnoisu.reader.ui.widget.page

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.woodnoisu.reader.model.BookBean
import com.woodnoisu.reader.ui.widget.page.anim.*
import com.woodnoisu.reader.ui.widget.page.event.OnCurPageChangeListener
import com.woodnoisu.reader.ui.widget.page.model.Direction
import com.woodnoisu.reader.ui.widget.page.model.PageMode
import kotlin.math.abs

/**
 * 原作者的GitHub Project Path:(https://github.com/PeachBlossom/treader)
 * 绘制页面显示内容的类
 */
class PageView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    // 当前View的宽
    private var mViewWidth = 0

    // 当前View的高
    private var mViewHeight = 0

    // 开始位置x
    private var mStartX = 0

    // 开始位置y
    private var mStartY = 0

    //是否移动
    private var isMove = false

    // 初始化参数
    private var mBgColor = -0x313d64

    //页面类型（默认为仿真翻页）
    private var mPageMode = PageMode.SIMULATION

    // 是否允许点击
    private var canTouch = true

    // 唤醒菜单的区域
    private var mCenterRect: RectF? = null

    // 是否准备完成
    private var isPrepare = false

    // 动画类
    private var mPageAnim: PageAnimation? = null

    //点击监听
    private var mOnTouchListener: com.woodnoisu.reader.ui.widget.page.event.OnTouchListener? = null

    //内容加载器
    private var mPageLoader: PageLoader? = null

    // 动画监听类
    private val mPageAnimListener =
        object : OnCurPageChangeListener {
            override fun hasPrev(): Boolean {
                return hasPrevPage()
            }

            override operator fun hasNext(): Boolean {
                return hasNextPage()
            }

            override fun pageCancel() {
                this@PageView.pageCancel()
            }
        }

    /**
     * 尺寸变化事件
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mViewWidth = w
        mViewHeight = h
        isPrepare = true
        mPageLoader?.prepareDisplay(w, h)
    }

    /**
     * 绘图事件
     */
    override fun onDraw(canvas: Canvas) {

        //绘制背景
        canvas.drawColor(mBgColor)

        //绘制动画
        mPageAnim?.draw(canvas)
    }

    /**
     * 触摸事件
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)
        if (!canTouch && event.action != MotionEvent.ACTION_DOWN) return true
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mStartX = x
                mStartY = y
                isMove = false
                canTouch = mOnTouchListener?.onTouch() ?: false
                mPageAnim?.onTouchEvent(event)
            }
            MotionEvent.ACTION_MOVE -> {
                // 判断是否大于最小滑动值。
                val slop = ViewConfiguration.get(context).scaledTouchSlop
                if (!isMove) {
                    isMove =
                        abs(mStartX - event.x) > slop || abs(
                            mStartY - event.y
                        ) > slop
                }

                // 如果滑动了，则进行翻页。
                if (isMove) {
                    mPageAnim?.onTouchEvent(event)
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!isMove) {
                    //设置中间区域范围
                    if (mCenterRect == null) {
                        mCenterRect = RectF(
                            (mViewWidth / 5).toFloat(), (mViewHeight / 3).toFloat(),
                            (mViewWidth * 4 / 5).toFloat(), (mViewHeight * 2 / 3).toFloat()
                        )
                    }

                    //是否点击了中间
                    if (mCenterRect?.contains(x.toFloat(), y.toFloat()) == true) {
                        mOnTouchListener?.center()
                        return true
                    }
                }
                mPageAnim?.onTouchEvent(event)
            }
        }
        return true
    }

    /**
     * 滚动事件
     */
    override fun computeScroll() {
        //进行滑动
        mPageAnim?.scrollAnim()
        super.computeScroll()
    }

    /**
     * 独立于窗体事件
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mPageAnim?.abortAnim()
        mPageAnim?.clear()
        mPageLoader = null
        mPageAnim = null
    }

    /**
     * 是否准备完成
     */
    fun isPrepare(): Boolean {
        return isPrepare
    }

    /**
     * 是否在运行
     */
    fun isRunning(): Boolean {
        return mPageAnim?.isRunning == true
    }

    /**
     * 获取背景图片
     */
    fun getBgBitmap(): Bitmap? {
        return mPageAnim?.getBgBitmap()
    }

    /**
     * 获取 PageLoader
     */
    fun getPageLoader(collBook: BookBean): PageLoader {
        // 判是否已经存在
        if (mPageLoader != null) {
            return mPageLoader!!
        }
        // 根据书籍类型，获取具体的加载器
        mPageLoader = if (collBook.bookFilePath.isNotBlank()) {
            LocalPageLoader(this, collBook)
        } else {
            NetPageLoader(this, collBook)
        }
        // 判断是否 PageView 已经初始化完成
        if (mViewWidth != 0 || mViewHeight != 0) {
            // 初始化 PageLoader 的屏幕大小
            mPageLoader?.prepareDisplay(mViewWidth, mViewHeight)
        }
        return mPageLoader!!
    }

    /**
     * 设置触摸事件
     */
    fun setTouchListener(mOnTouchListener: com.woodnoisu.reader.ui.widget.page.event.OnTouchListener?) {
        this.mOnTouchListener = mOnTouchListener
    }

    /**
     * 设置翻页的模式
     */
    fun setPageMode(pageMode: PageMode) {
        mPageMode = pageMode
        //视图未初始化的时候，禁止调用
        if (mViewWidth == 0 || mViewHeight == 0) return
        mPageAnim = when (mPageMode) {
            PageMode.SIMULATION -> SimulationPageAnim(
                mViewWidth,
                mViewHeight,
                this,
                mPageAnimListener
            )
            PageMode.COVER -> CoverPageAnim(mViewWidth, mViewHeight, this, mPageAnimListener)
            PageMode.SLIDE -> SlidePageAnim(mViewWidth, mViewHeight, this, mPageAnimListener)
            PageMode.NONE -> NonePageAnim(mViewWidth, mViewHeight, this, mPageAnimListener)
            PageMode.SCROLL -> ScrollPageAnim(
                mViewWidth, mViewHeight, 0,
                mPageLoader?.getMarginHeight() ?: 0, this, mPageAnimListener
            )
        }
    }

    /**
     * 设置背景色
     */
    fun setBgColor(color: Int) {
        mBgColor = color
    }

    /**
     * 自动上一页
     */
    fun autoPrevPage(): Boolean {
        //滚动暂时不支持自动翻页
        return if (mPageAnim is ScrollPageAnim) {
            false
        } else {
            startPageAnim(Direction.PRE)
            true
        }
    }

    /**
     * 自动下一页
     */
    fun autoNextPage(): Boolean {
        return if (mPageAnim is ScrollPageAnim) {
            false
        } else {
            startPageAnim(Direction.NEXT)
            true
        }
    }

    /**
     * 绘制下一页
     */
    fun drawNextPage() {
        if (!isPrepare) return
        if (mPageAnim is HorizonPageAnim) {
            (mPageAnim as HorizonPageAnim).changePage()
        }
        val bmp = getNextBitmap()
        if (bmp != null) {
            mPageLoader?.drawPage(bmp, false)
        }
    }

    /**
     * 绘制当前页。
     */
    fun drawCurPage(isUpdate: Boolean) {
        if (!isPrepare) return
        if (!isUpdate) {
            if (mPageAnim is ScrollPageAnim) {
                (mPageAnim as ScrollPageAnim).resetBitmap()
            }
        }
        val bmp = getNextBitmap()
        if (bmp != null) {
            mPageLoader?.drawPage(bmp, isUpdate)
        }
    }

    /**
     * 判断是否存在上一页
     */
    private fun hasPrevPage(): Boolean {
        mOnTouchListener?.prePage()
        return mPageLoader?.prev() == true
    }

    /**
     * 判断是否下一页存在
     */
    private fun hasNextPage(): Boolean {
        mOnTouchListener?.nextPage()
        return mPageLoader?.next() == true
    }

    /**
     * 获取下一张图
     */
    private fun getNextBitmap(): Bitmap? {
        return mPageAnim?.getNextBitmap()
    }

    /**
     * 开始动画
     */
    private fun startPageAnim(direction: Direction) {
        if (mOnTouchListener == null) return
        //是否正在执行动画
        abortAnimation()
        if (direction == Direction.NEXT) {
            val x = mViewWidth
            val y = mViewHeight
            //初始化动画
            mPageAnim?.setStartPoint(x.toFloat(), y.toFloat())
            //设置点击点
            mPageAnim?.setTouchPoint(x.toFloat(), y.toFloat())
            //设置方向
            val hasNext = hasNextPage()
            mPageAnim?.setDirection(direction)
            if (!hasNext) {
                return
            }
        } else {
            val x = 0
            val y = mViewHeight
            //初始化动画
            mPageAnim?.setStartPoint(x.toFloat(), y.toFloat())
            //设置点击点
            mPageAnim?.setTouchPoint(x.toFloat(), y.toFloat())
            mPageAnim?.setDirection(direction)
            //设置方向方向
            val hashPrev = hasPrevPage()
            if (!hashPrev) {
                return
            }
        }
        mPageAnim?.startAnim()
        this.postInvalidate()
    }

    /**
     * 如果滑动状态没有停止就取消状态，重新设置Anim的触碰点
     */
    private fun abortAnimation() {
        mPageAnim?.abortAnim()
    }

    /**
     * 页面取消
     */
    private fun pageCancel() {
        mOnTouchListener?.cancel()
        mPageLoader?.pageCancel()
    }
}

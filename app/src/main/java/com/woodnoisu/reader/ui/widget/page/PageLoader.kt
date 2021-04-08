package com.woodnoisu.reader.ui.widget.page

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.text.TextPaint
import androidx.core.content.ContextCompat
import com.woodnoisu.reader.R
import com.woodnoisu.reader.constant.Constant
import com.woodnoisu.reader.model.BookBean
import com.woodnoisu.reader.model.ChapterBean
import com.woodnoisu.reader.model.ReadRecordBean
import com.woodnoisu.reader.ui.widget.page.event.OnPageChangeListener
import com.woodnoisu.reader.ui.widget.page.model.PageMode
import com.woodnoisu.reader.ui.widget.page.model.PageStyle
import com.woodnoisu.reader.ui.widget.page.model.TxtPage
import com.woodnoisu.reader.ui.widget.page.utils.DateUtil
import com.woodnoisu.reader.utils.*
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.jvm.Throws

abstract class PageLoader(pageView: PageView,
                          collBook: BookBean
){
    //协程作用域
    protected val uiScope = CoroutineScope(Dispatchers.Main)

    // 书本对象
    protected val mCollBook: BookBean = collBook

    // 监听器
    protected var mPageChangeListener: OnPageChangeListener? = null

    // 页面显示类
    private val mPageView: PageView = pageView

    // 上下文
    private val mContext: Context = pageView.context

    // 当前显示的页
    private var mCurPage: TxtPage? = null

    // 当前章节列表
    protected val mChapterList: MutableList<ChapterBean> = ArrayList(1)

    //将章节数据，解析成页面列表 chapter：章节信息 br：章节的文本流
    private val pics: MutableList<String> = ArrayList()

    // 上一章的页面列表缓存
    private val mPrePageList: MutableList<TxtPage> = ArrayList()

    // 当前章节的页面列表
    private val mCurPageList: MutableList<TxtPage> = ArrayList()

    // 下一章的页面列表缓存
    protected val mNextPageList: MutableList<TxtPage> = ArrayList()

    // 绘制电池的画笔
    private lateinit var mBatteryPaint: Paint

    // 绘制提示的画笔
    private lateinit var mTipPaint: Paint

    // 绘制标题的画笔
    private lateinit var mTitlePaint: Paint

    // 绘制背景颜色的画笔(用来擦除需要重绘的部分)
    private lateinit var mBgPaint: Paint

    // 绘制小说内容的画笔
    private lateinit var mTextPaint: TextPaint
    private lateinit var mSelectPaint: Paint

    // 阅读器的配置选项
    private val mSettingManager = ReadSettingManager.getInstance()

    // 被遮盖的页，或者认为被取消显示的页
    private var mCancelPage: TxtPage? = null

    // 存储阅读记录类
    protected lateinit var mBookRecord: ReadRecordBean

    /***************** params *****************/
    // 当前的状态
    protected var mStatus: Int = STATUS_LOADING

    // 判断章节列表是否加载完成
    protected var isChapterListPrepare = false

    // 是否打开过章节
    private var isChapterOpen = false
    private var isFirstOpen = true
    private var isClose = false

    // 页面的翻页效果模式
    private var mPageMode = mSettingManager.pageMode

    // 加载器的颜色主题
    private var mPageStyle = mSettingManager.pageStyle

    //当前是否是夜间模式
    private var isNightMode = false

    //书籍绘制区域的宽高
    private var mVisibleWidth = 0
    private var mVisibleHeight = 0

    //应用的宽高
    private var mDisplayWidth = 0
    private var mDisplayHeight = 0

    //间距
    private var mMarginWidth = 0
    private var mMarginHeight = 0

    //字体的颜色
    private var mTextColor = 0

    //标题的大小
    private var mTitleSize = 0

    //字体的大小
    private var mTextSize = 0

    //行间距
    private var mTextInterval = 0

    //标题的行间距
    private var mTitleInterval = 0

    //段落距离(基于行间距的额外距离)
    private var mTextPara = 0

    //标题距离
    private var mTitlePara = 0

    //电池的百分比
    private var mBatteryLevel = 0

    //当前页面的背景
    private var mBgColor = 0

    // 当前章
    protected var mCurChapterPos = 0

    //上一章的记录
    private var mLastChapterPos = 0

//    //异步任务
//    private var asyncTask: AsyncTask<Int, Void, MutableList<TxtPage>>? = null

    // 预加载任务
    private var asyncJob: Job? = null

    /**
     * 静态内容
     */
    companion object {
        // 当前页面的状态
        const val STATUS_LOADING = 1 // 正在加载

        const val STATUS_FINISH = 2 // 加载完成

        const val STATUS_ERROR = 3 // 加载错误 (一般是网络加载情况)

        const val STATUS_EMPTY = 4 // 空数据

        const val STATUS_PARING = 5 // 正在解析 (装载本地数据)

        const val STATUS_PARSE_ERROR = 6 // 本地文件解析错误(暂未被使用)

        const val STATUS_CATEGORY_EMPTY = 7 // 获取到的目录为空

        // 默认的显示参数配置
        private const val DEFAULT_MARGIN_HEIGHT = 45
        private const val DEFAULT_MARGIN_WIDTH = 15
        private const val DEFAULT_TIP_SIZE = 12
        private const val EXTRA_TITLE_SIZE = 4
    }

    /**
     * 初始化
     */
    init {
        // 初始化数据
        initData()
        // 初始化画笔
        initPaint()
        // 初始化PageView
        initPageView()
        // 初始化书籍
        initBook()
    }

    /**
     * 跳转到指定章节
     *
     * @param pos:从 0 开始。
     */
    fun skipToChapter(pos: Int) {
        // 设置参数
        mCurChapterPos = pos

        // 将上一章的缓存设置为null
        mPrePageList.clear()
        // 如果当前下一章缓存正在执行，则取消
        asyncJob?.cancel()
        //asyncTask?.cancel(true)
        // 将下一章缓存设置为null
        mNextPageList.clear()

        // 打开指定章节
        openChapter()
    }

    /**
     * 翻到上一页
     */
    fun skipToPrePage(): Boolean {
        return mPageView.autoPrevPage()
    }

    /**
     * 翻到下一页
     */
    fun skipToNextPage(): Boolean {
        return mPageView.autoNextPage()
    }

    /**
     * 更新时间
     */
    fun updateTime() {
        if (!mPageView.isRunning()) {
            mPageView.drawCurPage(true)
        }
    }

    /**
     * 更新电量
     *
     * @param level
     */
    fun updateBattery(level: Int) {
        mBatteryLevel = level
        if (!mPageView.isRunning()) {
            mPageView.drawCurPage(true)
        }
    }

    /**
     * 设置文字相关参数
     *
     * @param textSize
     */
    fun setTextSize(textSize: Int) {
        // 设置文字相关参数
        setUpTextParams(textSize)

        // 设置画笔的字体大小
        mTextPaint.textSize = mTextSize.toFloat()
        mSelectPaint.textSize = mTextSize.toFloat()
        // 设置标题的字体大小
        mTitlePaint.textSize = mTitleSize.toFloat()
        // 存储文字大小
        mSettingManager.textSize = mTextSize
        // 取消缓存
        mPrePageList.clear()
        mNextPageList.clear()

        // 如果当前已经显示数据
        if (isChapterListPrepare && mStatus == STATUS_FINISH) {
            // 重新计算当前页面
            dealLoadPageList(mCurChapterPos)

            // 防止在最后一页，通过修改字体，以至于页面数减少导致崩溃的问题
            if (!mCurPageList.isNullOrEmpty()) {
                var p = mCurPage?.position ?: 0
                if (p >= mCurPageList.size) {
                    p = mCurPageList.size - 1
                }

                // 重新获取指定页面
                mCurPage = mCurPageList[p]
            }
        }
        mPageView.drawCurPage(false)
    }

    /**
     * 设置夜间模式
     *
     * @param nightMode
     */
    fun setNightMode(nightMode: Boolean) {
        mSettingManager.isNightMode = nightMode
        isNightMode = nightMode
        if (isNightMode) {
            mBatteryPaint.color = Color.WHITE
            setPageStyle(PageStyle.NIGHT)
        } else {
            mBatteryPaint.color = Color.BLACK
            setPageStyle(mPageStyle)
        }
    }

    /**
     * 设置页面样式
     *
     * @param pageStyle:页面样式
     */
    fun setPageStyle(pageStyle: PageStyle) {
        if (pageStyle !== PageStyle.NIGHT) {
            mPageStyle = pageStyle
            mSettingManager.pageStyle = pageStyle
        }
        if (isNightMode && pageStyle !== PageStyle.NIGHT) {
            return
        }

        // 设置当前颜色样式
        mTextColor =
            ContextCompat.getColor(mContext, pageStyle.fontColor)
        mBgColor = ContextCompat.getColor(mContext, pageStyle.bgColor)
        mTipPaint.color = mTextColor
        mTitlePaint.color = mTextColor
        mTextPaint.color = mTextColor
        mBgPaint.color = mBgColor
        mPageView.drawCurPage(false)
    }

    /**
     * 翻页动画
     *
     * @param pageMode:翻页模式
     * @see PageMode
     */
    fun setPageMode(pageMode: PageMode) {
        mPageMode = pageMode
        mPageView.setPageMode(mPageMode)
        mSettingManager.pageMode = mPageMode

        // 重新绘制当前页
        mPageView.drawCurPage(false)
    }

    /**
     * 设置页面切换监听
     *
     * @param listener
     */
    fun setOnPageChangeListener(listener: OnPageChangeListener) {
        mPageChangeListener = listener

        // 如果目录加载完之后才设置监听器，那么会默认回调
        if (isChapterListPrepare) {
            mPageChangeListener?.onChaptersFinished(mChapterList)
        }
    }

    /**
     * 设置阅读记录
     */
    fun setBookRecord(bookRecord: ReadRecordBean) {
        mBookRecord = bookRecord
        mCurChapterPos = mBookRecord.chapterPos
        mLastChapterPos = mCurChapterPos
    }

    /**
     * 获取当前页的状态
     *
     * @return
     */
    fun getPageStatus(): Int {
        return mStatus
    }

    /**
     * 获取书籍信息
     *
     * @return
     */
    fun getCollBook(): BookBean {
        return mCollBook
    }

    /**
     * 获取当前章节的章节位置
     */
    fun getChapterPos(): Int {
        return mCurChapterPos
    }

    /**
     * 获取距离屏幕的高度
     */
    fun getMarginHeight(): Int {
        return mMarginHeight
    }

    /**
     * 获取当前的阅读记录
     */
    fun getRecord(): ReadRecordBean {
        if (mChapterList.isNotEmpty()) {

            mBookRecord.bookUrl = mCollBook.url
            mBookRecord.chapterPos = mCurChapterPos
            val p = mCurPage?.position ?: 0
            mBookRecord.pagePos = p
            mBookRecord.lastRead = System.currentTimeMillis().toString()
        }
        return mBookRecord
    }

    /**
     * 打开指定章节
     */
    fun openChapter() {
        if (isClose) {
            return
        }
        isFirstOpen = false
        if (!mPageView.isPrepare()) {
            return
        }

        // 如果章节目录没有准备好
        if (!isChapterListPrepare) {
            mStatus = STATUS_LOADING
            mPageView.drawCurPage(false)
            return
        }

        // 如果获取到的章节目录为空
        if (mChapterList.isEmpty()) {
            mStatus = STATUS_CATEGORY_EMPTY
            mPageView.drawCurPage(false)
            return
        }
        if (parseCurChapter()) {
            // 如果章节从未打开
            if (!isChapterOpen) {
                var position: Int = mBookRecord.pagePos

                // 防止记录页的页号，大于当前最大页号
                if (position >= mCurPageList.size) {
                    position = mCurPageList.size - 1
                }
                mCurPage = getCurPage(position)
                mCancelPage = mCurPage
                // 切换状态
                isChapterOpen = true
            } else {
                mCurPage = getCurPage(0)
            }
        } else {
            mCurPage = TxtPage()
        }
        mPageView.drawCurPage(false)
    }

    /**
     * 章节错误
     */
    fun chapterError() {
        //加载错误
        mStatus = STATUS_ERROR
        mPageView.drawCurPage(false)
    }

    /**
     * 关闭书本
     */
    fun closeBook() {
        isChapterListPrepare = false
        isClose = true
        //asyncTask?.cancel(false)
        mChapterList.clear()
        mCurPageList.clear()
        mNextPageList.clear()
        //mPageView = null
        mCurPage = null
        // 取消page域下的所有协程
        uiScope.cancel()
    }

    /**
     * 章节是否已打开
     */
    fun isChapterOpen(): Boolean {
        return isChapterOpen
    }

    /**
     * 绘制页面
     */
    fun drawPage(bitmap: Bitmap, isUpdate: Boolean) {
        val bmp = mPageView.getBgBitmap()
        if (bmp != null) {
            drawBackground(bmp, isUpdate)
            if (!isUpdate) {
                drawContent(bitmap)
            }
            //更新绘制
            mPageView.invalidate()
        }
    }

    /**
     * 预先绘制
     */
    fun prepareDisplay(w: Int, h: Int) {
        // 获取PageView的宽高
        mDisplayWidth = w
        mDisplayHeight = h

        // 获取内容显示位置的大小
        mVisibleWidth = mDisplayWidth - mMarginWidth * 2
        mVisibleHeight = mDisplayHeight - mMarginHeight * 2

        // 重置 PageMode
        mPageView.setPageMode(mPageMode)
        if (!isChapterOpen) {
            // 展示加载界面
            mPageView.drawCurPage(false)
            // 如果在 display 之前调用过 openChapter 肯定是无法打开的。
            // 所以需要通过 display 再重新调用一次。
            if (!isFirstOpen) {
                // 打开书籍
                openChapter()
            }
        } else {
            // 如果章节已显示，那么就重新计算页面
            if (mStatus == STATUS_FINISH) {
                dealLoadPageList(mCurChapterPos)
                // 重新设置文章指针的位置
                val p = mCurPage?.position ?: 0
                mCurPage = getCurPage(p)
            }
            mPageView.drawCurPage(false)
        }
    }

    /**
     * 翻阅上一页
     */
    fun prev(): Boolean {
        // 以下情况禁止翻页
        if (!canTurnPage()) {
            return false
        }
        if (mStatus == STATUS_FINISH) {
            // 先查看是否存在上一页
            val prevPage: TxtPage? = getPrevPage()
            if (prevPage != null) {
                mCancelPage = mCurPage
                mCurPage = prevPage
                mPageView.drawNextPage()
                return true
            }
        }
        if (!hasPrevChapter()) {
            return false
        }
        mCancelPage = mCurPage
        mCurPage = if (parsePrevChapter()) {
            getPrevLastPage()
        } else {
            TxtPage()
        }
        mPageView.drawNextPage()
        return true
    }

    /**
     * 翻到下一页
     *
     * @return :是否允许翻页
     */
    fun next(): Boolean {
        // 以下情况禁止翻页
        if (!canTurnPage()) {
            return false
        }
        if (mStatus == STATUS_FINISH) {
            // 先查看是否存在下一页
            val nextPage: TxtPage? = getNextPage()
            if (nextPage != null) {
                mCancelPage = mCurPage
                mCurPage = nextPage
                mPageView.drawNextPage()
                return true
            }
        }
        if (!hasNextChapter()) {
            return false
        }
        mCancelPage = mCurPage
        // 解析下一章数据
        mCurPage = if (parseNextChapter()) {
            mCurPageList[0]
        } else {
            TxtPage()
        }
        mPageView.drawNextPage()
        return true
    }

    /**
     * 取消翻页
     */
    fun pageCancel() {
        val p = mCurPage?.position ?: 0
        if (p == 0 && mCurChapterPos > mLastChapterPos) {
            // 加载到下一章取消了
            if (!mPrePageList.isNullOrEmpty()) {
                cancelNextChapter()
            } else {
                mCurPage = if (parsePrevChapter()) {
                    getPrevLastPage()
                } else {
                    TxtPage()
                }
            }
        } else if (mCurPageList.isNullOrEmpty()
            || (p == mCurPageList.size - 1 && mCurChapterPos < mLastChapterPos)
        ) {  // 加载上一章取消了
            if (!mNextPageList.isNullOrEmpty()) {
                cancelPreChapter()
            } else {
                mCurPage = if (parseNextChapter()) {
                    mCurPageList[0]
                } else {
                    TxtPage()
                }
            }
        } else {
            // 假设加载到下一页，又取消了。那么需要重新装载。
            mCurPage = mCancelPage
        }
    }

    /**
     * 预加载下一章
     */
    @SuppressLint("StaticFieldLeak")
    fun preLoadNextChapter() {
        val nextChapter = mCurChapterPos + 1

        // 如果不存在下一章，且下一章没有数据，则不进行加载。
        if (!hasNextChapter()
            || !hasChapterData(mChapterList[nextChapter])
        ) {
            return
        }
        preLoadNextChapter(nextChapter)
    }

    /**
     * 异步预加载下一章
     * */
    private fun  preLoadNextChapter(nextChapter:Int):CoroutineScope {
        //如果之前正在加载则取消
        if (asyncJob != null && asyncJob!!.isActive) {
            asyncJob?.cancel()
        }
        uiScope.launch(Dispatchers.IO) {
            try {
                // 获取预先加载内容
                var txtPages = loadPageList(nextChapter)
                // 获取成功后
                uiScope.launch(Dispatchers.Main) {
                    mNextPageList.clear()
                    mNextPageList.addAll(txtPages)
                }

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                LogUtil.e("PageLoader","异步预加载下一章失败,${e}")
            }
        }

        return uiScope
    }


    /**
     * 解析上一章数据
     *
     * @return :数据是否解析成功
     */
    open fun parsePrevChapterExt(){}

    /**
     * 解析当前章节
     */
    open fun parseCurChapterExt(){}

    /**
     * 解析下一章数据
     *
     * @return:返回解析成功还是失败
     */
    open fun parseNextChapterExt(){}

    /**
     * 刷新章节列表
     */
    abstract fun refreshChapterList()

    /**
     * 打开指定章节
     */
    abstract fun openSpecifyChapter(specifyChapter: Int)

    /**
     * 获取章节的文本流
     */
    @Throws(java.lang.Exception::class)
    protected abstract fun getChapterReader(chapter: ChapterBean): BufferedReader?

    /**
     * 章节数据是否存在
     */
    protected abstract fun hasChapterData(chapter: ChapterBean): Boolean

    /**
     * 初始化数据
     */
    private fun initData() {
        // 初始化参数
        mMarginWidth =
            ScreenUtil.dpToPx(DEFAULT_MARGIN_WIDTH)
        mMarginHeight =
            ScreenUtil.dpToPx(DEFAULT_MARGIN_HEIGHT)
        // 配置文字有关的参数
        setUpTextParams(mSettingManager.textSize)
    }

    /**
     * 初始化绘图相关
     */
    @SuppressLint("ResourceAsColor")
    private fun initPaint() {
        // 绘制提示的画笔
        mTipPaint = Paint()
        mTipPaint.color = mTextColor
        mTipPaint.textAlign = Paint.Align.LEFT // 绘制的起始点
        mTipPaint.textSize = ScreenUtil.spToPx(DEFAULT_TIP_SIZE).toFloat() // Tip默认的字体大小
        mTipPaint.isAntiAlias = true
        mTipPaint.isSubpixelText = true

        // 绘制页面内容的画笔
        mTextPaint = TextPaint()
        mTextPaint.color = mTextColor
        mTextPaint.textSize = mTextSize.toFloat()
        mTextPaint.isAntiAlias = true
        mSelectPaint = TextPaint()
        mSelectPaint.color = R.color.colorSelect //mContext.resources.getColor(R.color.colorSelect)
        mSelectPaint.textSize = mTextSize.toFloat()
        mSelectPaint.isAntiAlias = true

        // 绘制标题的画笔
        mTitlePaint = TextPaint()
        mTitlePaint.color = mTextColor
        mTitlePaint.textSize = mTitleSize.toFloat()
        mTitlePaint.style = Paint.Style.FILL_AND_STROKE
        mTitlePaint.typeface = Typeface.DEFAULT_BOLD
        mTitlePaint.isAntiAlias = true

        // 绘制背景的画笔
        mBgPaint = Paint()
        mBgPaint.color = mBgColor

        // 绘制电池的画笔
        mBatteryPaint = Paint()
        mBatteryPaint.isAntiAlias = true
        mBatteryPaint.isDither = true

        // 初始化页面样式
        setNightMode(mSettingManager.isNightMode)
    }

    /**
     * 初始页面
     */
    private fun initPageView() {
        //配置参数
        mPageView.setPageMode(mPageMode)
        mPageView.setBgColor(mBgColor)
    }

    /**
     * 初始化书籍
     */
    private fun initBook() {

    }

    /**
     * 作用：设置与文字相关的参数
     *
     * @param textSize
     */
    private fun setUpTextParams(textSize: Int) {
        // 文字大小
        mTextSize = textSize
        mTitleSize =
            mTextSize + ScreenUtil.spToPx(EXTRA_TITLE_SIZE)
        // 行间距(大小为字体的一半)
        mTextInterval = mTextSize / 2
        mTitleInterval = mTitleSize / 2
        // 段落间距(大小为字体的高度)
        mTextPara = mTextSize
        mTitlePara = mTitleSize
    }

    /**
     * 绘制背景
     */
    private fun drawBackground(bitmap: Bitmap, isUpdate: Boolean) {
        val canvas = Canvas(bitmap)
        val tipMarginHeight: Int = ScreenUtil.dpToPx(3)
        if (!isUpdate) {
            //绘制背景
            canvas.drawColor(mBgColor)
            if (mChapterList.isNotEmpty()) {
                //初始化标题的参数
                //需要注意的是:绘制text的y的起始点是text的基准线的位置，而不是从text的头部的位置
                val tipTop = tipMarginHeight - mTipPaint.fontMetrics.top
                //根据状态不一样，数据不一样
                if (mStatus != STATUS_FINISH) {
                    if (isChapterListPrepare) {
                        // 目前不清楚发生的情形,只能这样防止用户瞎逼操作导致数组越界
                        if (mChapterList.size > mCurChapterPos) {
                            canvas.drawText(
                                mChapterList[mCurChapterPos].name
                                , mMarginWidth.toFloat(), tipTop, mTipPaint
                            )
                        }
                    }
                } else {
                    val title = mCurPage?.title ?: ""
                    canvas.drawText(title, mMarginWidth.toFloat(), tipTop, mTipPaint)
                }

                //绘制页码
                // 底部的字显示的位置Y
                val y =
                    mDisplayHeight - mTipPaint.fontMetrics.bottom - tipMarginHeight
                // 只有finish的时候采用页码
                if (mStatus == STATUS_FINISH) {
                    val percent =
                        (mCurPage!!.position + 1).toString() + "/" + mCurPageList.size
                    canvas.drawText(percent, mMarginWidth.toFloat(), y, mTipPaint)
                }
            }
        } else {
            //擦除区域
            mBgPaint.color = mBgColor
            canvas.drawRect(
                (mDisplayWidth shr 1.toFloat().toInt()).toFloat(),
                mDisplayHeight.toFloat() - mMarginHeight + ScreenUtil.dpToPx(2),
                mDisplayWidth.toFloat(),
                mDisplayHeight.toFloat(),
                mBgPaint
            )
        }

        //绘制电池
        val visibleRight = mDisplayWidth - mMarginWidth
        val visibleBottom = mDisplayHeight - tipMarginHeight
        val outFrameWidth = mTipPaint.measureText("xxx").toInt()
        val outFrameHeight = mTipPaint.textSize.toInt()
        val polarHeight: Int = ScreenUtil.dpToPx(6)
        val polarWidth: Int = ScreenUtil.dpToPx(2)
        val border = 1
        val innerMargin = 1

        //电极的制作
        val polarLeft = visibleRight - polarWidth
        val polarTop = visibleBottom - (outFrameHeight + polarHeight) / 2
        val polar = Rect(
            polarLeft, polarTop, visibleRight,
            polarTop + polarHeight - ScreenUtil.dpToPx(2)
        )
        mBatteryPaint.style = Paint.Style.FILL
        canvas.drawRect(polar, mBatteryPaint)

        //外框的制作
        val outFrameLeft = polarLeft - outFrameWidth
        val outFrameTop = visibleBottom - outFrameHeight
        val outFrameBottom: Int = visibleBottom - ScreenUtil.dpToPx(2)
        val outFrame =
            Rect(outFrameLeft, outFrameTop, polarLeft, outFrameBottom)
        mBatteryPaint.style = Paint.Style.STROKE
        mBatteryPaint.strokeWidth = border.toFloat()
        canvas.drawRect(outFrame, mBatteryPaint)

        //内框的制作
        val innerWidth =
            (outFrame.width() - innerMargin * 2 - border) * (mBatteryLevel / 100.0f)
        val innerFrame = RectF(
            (outFrameLeft + border + innerMargin).toFloat(),
            (outFrameTop + border + innerMargin).toFloat(),
            outFrameLeft + border + innerMargin + innerWidth,
            (outFrameBottom - border - innerMargin).toFloat()
        )
        mBatteryPaint.style = Paint.Style.FILL
        canvas.drawRect(innerFrame, mBatteryPaint)

        //绘制当前时间
        //底部的字显示的位置Y
        val y = mDisplayHeight - mTipPaint.fontMetrics.bottom - tipMarginHeight
        val time: String = DateUtil.dateConvert(
            System.currentTimeMillis(),
            Constant.FORMAT_TIME
        )
        val x: Float =
            outFrameLeft - mTipPaint.measureText(time) - ScreenUtil.dpToPx(4)
        canvas.drawText(time, x, y, mTipPaint)
    }

    /**
     * 绘制内容
     */
    private fun drawContent(bitmap: Bitmap) {
        val canvas = Canvas(bitmap)
        if (mPageMode === PageMode.SCROLL) {
            canvas.drawColor(mBgColor)
        }

        //绘制内容
        if (mStatus != STATUS_FINISH) {
            //绘制字体
            var tip = ""
            when (mStatus) {
                STATUS_LOADING -> tip = "首次加载比较慢，请耐心等待..."
                STATUS_ERROR -> tip = "加载失败(点击边缘重试)"
                STATUS_EMPTY -> tip = "文章内容为空"
                STATUS_PARING -> tip = "正在排版请等待..."
                STATUS_PARSE_ERROR -> tip = "文件解析错误"
                STATUS_CATEGORY_EMPTY -> tip = "目录列表为空"
            }

            //将提示语句放到正中间
            drawCenter(tip, canvas)
        } else {
            var top: Float = if (mPageMode === PageMode.SCROLL) {
                -mTextPaint.fontMetrics.top
            } else {
                mMarginHeight - mTextPaint.fontMetrics.top
            }

            //设置总距离
            val interval = mTextInterval + mTextPaint.textSize.toInt()
            val para = mTextPara + mTextPaint.textSize.toInt()
            val titleInterval = mTitleInterval + mTitlePaint.textSize.toInt()
            val titlePara = mTitlePara + mTextPaint.textSize.toInt()
            var str: String

            //对标题进行绘制
            val titleLineCount = mCurPage?.titleLines ?: 0
            for (i in 0 until titleLineCount) {
                str = mCurPage?.lines?.get(i) ?: ""
                //设置顶部间距
                if (i == 0) {
                    top += mTitlePara.toFloat()
                }

                //计算文字显示的起始点
                val start = (mDisplayWidth - mTitlePaint.measureText(str)).toInt() / 2
                //进行绘制
                mTitlePaint.color = mTextColor
                canvas.drawText(str, start.toFloat(), top, mTitlePaint)

                //设置尾部间距
                top += if (i == mCurPage!!.titleLines - 1) {
                    titlePara.toFloat()
                } else {
                    //行间距
                    titleInterval.toFloat()
                }
            }

            //对内容进行绘制
            val lineCount = mCurPage?.lines?.size ?: 0
            for (i in titleLineCount until lineCount) {
                str = mCurPage?.lines?.get(i) ?: ""
                canvas.drawText(str, mMarginWidth.toFloat(), top, mTextPaint)
                top += if (str.endsWith("\n")) {
                    para.toFloat()
                } else {
                    interval.toFloat()
                }
            }

            val pic = mCurPage?.pic ?: ""
            //如果有插图的话加载插图
            //loadImage(pic, mContext, canvas)
        }
    }

//    /**
//     * 加载插图（异步）
//     */
//    private  fun loadImage(imageUrl:String,ct: Context,canvas:Canvas): CoroutineScope {
//        uiScope.launch(Dispatchers.IO) {
//            try {
//                if (!imageUrl.isBlank()) {
//                    Glide.with(ct).asBitmap().load(imageUrl).thumbnail(0.1f)
//                        .into(object : SimpleTarget<Bitmap?>() {
//                            override fun onLoadStarted(placeholder: Drawable?) {
//                                canvas.save()
//                                drawCenter(mContext.getString(R.string.pic_loading), canvas)
//                                canvas.restore()
//                            }
//
//                            override fun onResourceReady(
//                                resource: Bitmap,
//                                transition: Transition<in Bitmap?>?
//                            ) {
//                                var bitmap = resource
//                                if (resource.width > mDisplayWidth) {
//                                    bitmap = scaleBitmap(resource)
//                                }
//                                val pivotX =
//                                    (mDisplayWidth - bitmap.width shr 1.toFloat().toInt()).toFloat()
//                                val pivotY =
//                                    (mDisplayHeight - bitmap.height shr 1.toFloat()
//                                        .toInt()).toFloat()
//                                if (!imageUrl.isBlank()) {
//                                    canvas.drawBitmap(bitmap, pivotX, pivotY, mTextPaint)
//                                    mPageView.invalidate()
//                                }
//                            }
//                        })
//                }
//            } catch (e: Exception) {
//                val errorMsg = "插图加载失败。。。"
//                LogUtil.e("PageLoader", "加载插图失败,${e}")
//                canvas.save()
//                drawCenter(errorMsg, canvas)
//                canvas.restore()
//            }
//        }
//        return uiScope
//    }

    /**
     * 中心文字绘制
     */
    private fun drawCenter(tip: String, canvas: Canvas) {
        val fontMetrics = mTextPaint.fontMetrics
        val textHeight = fontMetrics.top - fontMetrics.bottom
        val textWidth = mTextPaint.measureText(tip)
        val pivotX = (mDisplayWidth - textWidth) / 2
        val pivotY = (mDisplayHeight - textHeight) / 2
        canvas.drawText(tip, pivotX, pivotY, mTextPaint)
    }

    /**
     * 图片缩放
     */
    private fun scaleBitmap(origin: Bitmap): Bitmap {
        val width = origin.width
        val height = origin.height
        val matrix = Matrix()
        matrix.preScale(0.5.toFloat(), 0.5.toFloat())
        val newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false)
        return if (newBM == origin) {
            newBM
        } else newBM
    }

    /**
     * 判断是否上一章节为空
     */
    private fun hasPrevChapter(): Boolean {
        //判断是否上一章节为空
        return mCurChapterPos - 1 >= 0
    }


    /**
     * 是否有下一章节
     */
    private fun hasNextChapter(): Boolean {
        // 判断是否到达目录最后一章
        return mCurChapterPos + 1 < mChapterList.size
    }

    /**
     * 取消下一章节
     */
    private fun cancelNextChapter() {
        val temp = mLastChapterPos
        mLastChapterPos = mCurChapterPos
        mCurChapterPos = temp
        mNextPageList.clear()
        mNextPageList.addAll(mCurPageList)
        mCurPageList.clear()
        mCurPageList.addAll(mPrePageList)
        mPrePageList.clear()
        chapterChangeCallback()
        mCurPage = getPrevLastPage()
        mCancelPage = null
    }

    /**
     * 取消上一章节
     */
    private fun cancelPreChapter() {
        // 重置位置点
        val temp = mLastChapterPos
        mLastChapterPos = mCurChapterPos
        mCurChapterPos = temp
        // 重置页面列表
        mPrePageList.clear()
        mPrePageList.addAll(mCurPageList)
        mCurPageList.clear()
        mCurPageList.addAll(mNextPageList)
        mNextPageList.clear()
        chapterChangeCallback()
        mCurPage = getCurPage(0)
        mCancelPage = null
    }

    /**
     * 解析上一章数据
     *
     * @return :数据是否解析成功
     */
    private fun parsePrevChapter(): Boolean {
        // 加载上一章数据
        val prevChapter = mCurChapterPos - 1
        mLastChapterPos = mCurChapterPos
        mCurChapterPos = prevChapter

        // 当前章缓存为下一章
        mNextPageList.clear()
        mNextPageList.addAll(mCurPageList)

        // 判断是否具有上一章缓存
        if (!mPrePageList.isNullOrEmpty()) {
            mCurPageList.clear()
            mCurPageList.addAll(mPrePageList)
            mPrePageList.clear()

            // 回调
            chapterChangeCallback()
        } else {
            dealLoadPageList(prevChapter)
        }
        parsePrevChapterExt()
        return !mCurPageList.isNullOrEmpty()
    }

    /**
     * 解析当前章节
     */
    private fun parseCurChapter(): Boolean {
        // 解析数据
        dealLoadPageList(mCurChapterPos)
        // 预加载下一页面
        preLoadNextChapter()
        parseCurChapterExt()
        return !mCurPageList.isNullOrEmpty()
    }

    /**
     * 解析下一章数据
     *
     * @return:返回解析成功还是失败
     */
    private fun parseNextChapter(): Boolean {
        val nextChapter = mCurChapterPos + 1
        mLastChapterPos = mCurChapterPos
        mCurChapterPos = nextChapter

        // 将当前章的页面列表，作为上一章缓存
        mPrePageList.clear()
        mPrePageList.addAll(mCurPageList)

        // 是否下一章数据已经预加载了
        if (!mNextPageList.isNullOrEmpty()) {
            mCurPageList.clear()
            mCurPageList.addAll(mNextPageList)
            mNextPageList.clear()
            // 回调
            chapterChangeCallback()
        } else {
            // 处理页面解析
            dealLoadPageList(nextChapter)
        }
        // 预加载下一页面
        preLoadNextChapter()
        parseNextChapterExt()
        return !mCurPageList.isNullOrEmpty()
    }

    /**
     * 章节变化回调
     */
    private fun chapterChangeCallback() {
        mPageChangeListener?.onChapterChange(mCurChapterPos)
        pics.clear()
        mPageChangeListener?.onPageCountChange(mCurPageList.size)
    }

    /**
     * 处理加载页面列表
     */
    private fun dealLoadPageList(chapterPos: Int) {
        try {
            val pl = loadPageList(chapterPos)
            if (!pl.isNullOrEmpty()) {
                mCurPageList.clear()
                mCurPageList.addAll(pl)
                if (mCurPageList.isEmpty()) {
                    mStatus = STATUS_EMPTY
                    // 添加一个空数据
                    val page = TxtPage()
                    page.lines.clear()
                    mCurPageList.add(page)
                } else {
                    mStatus = STATUS_FINISH
                }
            } else {
                mStatus = STATUS_LOADING
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            mCurPageList.clear()
            mStatus = STATUS_ERROR
        }
        // 回调
        chapterChangeCallback()
    }

    /**
     * 加载页面列表
     *
     * @param chapterPos:章节序号
     * @return
     */
    @Throws(Exception::class)
    private fun loadPageList(chapterPos: Int): MutableList<TxtPage> {
        // 获取章节
        val chapter = mChapterList[chapterPos]
        // 判断章节是否存在
        if (!hasChapterData(chapter)) {
            return ArrayList()
        }
        // 获取章节的文本流
        val reader: BufferedReader? = getChapterReader(chapter)
        return loadPages(chapter, reader)
    }

    /**
     * 加载页面
     */
    private fun loadPages(
        chapter: ChapterBean,
        br: BufferedReader?
    ): MutableList<TxtPage> {
        //生成的页面
        val pages: MutableList<TxtPage> = ArrayList()
        //使用流的方式加载
        val lines: MutableList<String> = ArrayList()

        var rHeight = mVisibleHeight
        var titleLinesCount = 0
        var showTitle = true // 是否展示标题
        var paragraph: String = chapter.name //默认展示标题
        val title: String = StringUtil.convertCC(chapter.name)
        var half: String
        try {
            while (showTitle || br?.readLine()?.also { paragraph = it } != null) {
                half = paragraph
                paragraph = StringUtil.convertCC(paragraph)
                // 重置段落
                if (!showTitle) {
                    paragraph = paragraph.replace("\\s".toRegex(), "")
                    // 如果只有换行符，那么就不执行
                    if (paragraph == "") continue
                    paragraph = StringUtil.halfToFull("  $paragraph\n")
                } else {
                    //设置 title 的顶部间距
                    rHeight -= mTitlePara
                }
                var wordCount: Int
                var subStr: String
                while (paragraph.isNotEmpty()) {
                    //当前空间，是否容得下一行文字
                    rHeight -= if (showTitle) {
                        mTitlePaint.textSize.toInt()
                    } else {
                        mTextPaint.textSize.toInt()
                    }

                    // 一页已经填充满了，创建 TextPage
                    if (rHeight <= 0) {
                        // 创建Page
                        val page = TxtPage()
                        page.position = pages.size
                        page.title = title
                        page.lines.clear()
                        page.lines.addAll(lines)
                        page.titleLines = titleLinesCount
                        pages.add(page)
                        // 重置Lines
                        lines.clear()
                        rHeight = mVisibleHeight
                        titleLinesCount = 0
                        continue
                    }

                    //测量一行占用的字节数
                    wordCount = if (showTitle) {
                        mTitlePaint.breakText(
                            paragraph,
                            true, mVisibleWidth.toFloat(), null
                        )
                    } else {
                        mTextPaint.breakText(
                            paragraph,
                            true, mVisibleWidth.toFloat(), null
                        )
                    }
                    subStr = paragraph.substring(0, wordCount)
                    if (subStr != "\n") {
                        //将一行字节，存储到lines中
                        lines.add(subStr)

                        //设置段落间距
                        if (showTitle) {
                            titleLinesCount += 1
                            rHeight -= mTitleInterval
                        } else {
                            rHeight -= mTextInterval
                        }
                    }
                    //裁剪
                    paragraph = paragraph.substring(wordCount)
                }

                //增加段落的间距
                if (!showTitle && lines.size != 0) {
                    rHeight = rHeight - mTextPara + mTextInterval
                }
                if (showTitle) {
                    rHeight = rHeight - mTitlePara + mTitleInterval
                    showTitle = false
                }
                pics.addAll(getImages(half))
            }
            if (lines.size != 0) {
                //创建Page
                val page = TxtPage()
                page.position = pages.size
                page.title = title
                page.lines.addAll(lines)
                page.titleLines = titleLinesCount
                pages.add(page)
                //重置Lines
                lines.clear()
            }
            if (pics.size > 0) {
                for (i in pics.indices) {
                    val page = TxtPage()
                    page.position = pages.size
                    page.title = title
                    page.lines.clear()
                    page.titleLines = 0
                    page.pic = pics[i]
                    pages.add(page)
                    // 重置Lines
                    lines.clear()
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            br?.close()
        }
        return pages
    }

    /**
     * 获取图片
     */
    private fun getImages(content: String): List<String> {
        var img: String
        val pImage: Pattern
        val mImage: Matcher
        val images: MutableList<String> = ArrayList()
        val regExImg = "(<img.*src\\s*=\\s*(.*?)[^>]*?>)"
        pImage =
            Pattern.compile(regExImg, Pattern.CASE_INSENSITIVE)
        mImage = pImage.matcher(content)
        while (mImage.find()) {
            img = mImage.group()
            val m =
                Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)").matcher(img)
            while (m.find()) {
                val tempSelected = m.group(1)
                if (tempSelected != null) {
                    images.add(tempSelected)
                }
            }
        }
        return images
    }

    /**
     * @return :获取初始显示的页面
     */
    private fun getCurPage(pos: Int): TxtPage? {
        mPageChangeListener?.onPageChange(pos)
        return when {
            mCurPageList.isNullOrEmpty() -> null
            pos < 0 -> null
            pos >= mCurPageList.size -> {
                mCurPageList[mCurPageList.size - 1]
            }
            else -> mCurPageList[pos]
        }
    }

    /**
     * @return :获取上一个页面
     */
    private fun getPrevPage(): TxtPage? {
        val pos = (mCurPage?.position ?: 0) - 1
        if (pos < 0) {
            return null
        }
        mPageChangeListener?.onPageChange(pos)
        return mCurPageList[pos]
    }

    /**
     * @return :获取下一的页面
     */
    private fun getNextPage(): TxtPage? {
        val pos = (mCurPage?.position ?: 0) + 1
        if (pos >= mCurPageList.size) {
            return null
        }
        mPageChangeListener?.onPageChange(pos)
        return mCurPageList[pos]
    }

    /**
     * @return :获取上一个章节的最后一页
     */
    private fun getPrevLastPage(): TxtPage? {
        return if (!mCurPageList.isNullOrEmpty()) {
            val pos = mCurPageList.size - 1
            mPageChangeListener?.onPageChange(pos)
            mCurPageList[pos]
        } else {
            null
        }
    }

    /**
     * 根据当前状态，决定是否能够翻页
     */
    private fun canTurnPage(): Boolean {
        if (!isChapterListPrepare) {
            return false
        }
        if (mStatus == STATUS_PARSE_ERROR
            || mStatus == STATUS_PARING
        ) {
            return false
        } else if (mStatus == STATUS_ERROR) {
            mStatus = STATUS_LOADING
        }
        return true
    }
}
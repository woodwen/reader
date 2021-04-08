package com.woodnoisu.reader.ui.novelRead

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.KeyEvent
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AbsListView
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.woodnoisu.reader.constant.Constant
import com.woodnoisu.reader.constant.Constant.RESULT_IS_COLLECTED
import com.woodnoisu.reader.R
import com.woodnoisu.reader.base.BaseActivity
import com.woodnoisu.reader.ui.widget.page.ReadSettingDialog
import com.woodnoisu.reader.ui.widget.page.PageLoader
import com.woodnoisu.reader.ui.widget.page.ReadSettingManager
import com.woodnoisu.reader.ui.widget.page.event.OnPageChangeListener
import com.woodnoisu.reader.ui.widget.page.event.OnTouchListener
import com.woodnoisu.reader.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_read.*
import kotlinx.android.synthetic.main.layout_download.*
import kotlinx.android.synthetic.main.layout_light.*
import kotlinx.android.synthetic.main.layout_read_mark.*
import java.util.*
import androidx.lifecycle.Observer
import com.woodnoisu.reader.model.*

/**
 * 阅读页📕
 */
@AndroidEntryPoint
class NovelReadActivity : BaseActivity() {
    // vm
    @VisibleForTesting
    val viewModel: NovelReadViewModel by viewModels()

    // 工具栏进入动画
    private lateinit var mTopInAnim: Animation
    // 工具栏退出动画
    private lateinit var mTopOutAnim: Animation
    // 底部退出动画
    private lateinit var mBottomInAnim: Animation
    // 底部进入动画
    private lateinit var mBottomOutAnim: Animation
    // 章节适配器
    private lateinit var mCategoryAdapter: CatalogueAdapter
    // 标记适配器
    private lateinit var mMarkAdapter: MarkAdapter
    // 页面加载器
    private lateinit var mPageLoader: PageLoader
    // 阅读设置弹出框
    private lateinit var mSettingDialog: ReadSettingDialog
    // 接收电池信息和时间更新的广播
    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Objects.requireNonNull(intent.action) == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra("level", 0)
                mPageLoader.updateBattery(level)
            } else if (intent.action == Intent.ACTION_TIME_TICK) {
                mPageLoader.updateTime()
            }// 监听分钟的变化
        }
    }

    /**
     * 键盘事件
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (viewModel.canTurnPageByVolume()) {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> return mPageLoader.skipToPrePage()

                KeyEvent.KEYCODE_VOLUME_DOWN -> return mPageLoader.skipToNextPage()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 恢复焦点
     */
    override fun onResume() {
        super.onResume()
        setTheme()
    }

    /**
     * 后台处理
     */
    override fun onBackPressed() {
        super.onBackPressed()
        if (read_abl_top_menu.isVisible) {
            // 非全屏下才收缩，全屏下直接退出
            if (!viewModel.getIsFullScreen()) {
                toggleMenu(true)
                return
            }
        } else if (mSettingDialog.isShowing) {
            mSettingDialog.dismiss()
            return
        } else if (read_dl_slide.isDrawerOpen(GravityCompat.START)) {
            read_dl_slide.closeDrawer(GravityCompat.START)
            return
        }
//        val mCollBook = viewModel.getCollBook()
//        if (mCollBook.favorite == 0 && mCollBook.chapters.isNotEmpty()) {
//            val alertDialog = AlertDialog.Builder(this)
//                .setTitle(getString(R.string.add_book))
//                .setMessage(getString(R.string.like_book))
//                .setPositiveButton(getString(R.string.sure)) { dialog, which ->
//                    //设置为已收藏
//                    mCollBook.favorite = 1
//                    //设置阅读时间
//                    //mCollBook.lastRead = System.currentTimeMillis().toString()
//                    mWorker.saveCollBook(mCollBook)
//                    //bookRepository.saveCollBookWithAsync(mCollBook)
//                    //mCollBook.favorite = 1
//                    exit()
//                }
//                .setNegativeButton(getString(R.string.cancel)) { dialog, which -> exit() }.create()
//            alertDialog.show()
//        } else {
//            finish()
//        }
    }

    /**
     * 暂停
     */
    override fun onPause() {
        super.onPause()
        //存储阅读记录
        viewModel.saveBookRecord(mPageLoader.getRecord())
    }

    /**
     * 销毁
     */
    override fun onDestroy() {
        super.onDestroy()
        mSettingDialog.dismiss()
        mPageLoader.closeBook()
        unregisterReceiver(mReceiver)
    }

    /**
     * 附加到base上下文事件
     */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocalManageUtil.setLocal(newBase))
    }

    /**
     * 当前id
     */
    override fun getRLayout(): Int = R.layout.activity_read

    /**
     * 初始化窗口
     */
    override fun initView() {
        // 初始化动画
        mTopInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_top_in)
        mTopOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_top_out)
        mBottomInAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_in)
        mBottomOutAnim = AnimationUtils.loadAnimation(this, R.anim.slide_bottom_out)
        //退出的速度要快
        mTopOutAnim.duration = 200
        mBottomOutAnim.duration = 200

        // 设置状态栏风格
        StatusBarUtil.setBarsStyle(this, R.color.colorPrimary, true)

        val book = intent.getParcelableExtra<BookBean>(EXTRA_COLL_BOOK)
        // 初始化书籍
        viewModel.setCollBook(book!!)

        //保持屏幕亮
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        //获取页面加载器
        mPageLoader = read_pv_page.getPageLoader(viewModel.getCollBook())

        // 阅读设置器
        mSettingDialog = ReadSettingDialog(
            this,
            mPageLoader
        )

        //禁止滑动展示DrawerLayout
        read_dl_slide.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        //侧边打开后，返回键能够起作用
        read_dl_slide.isFocusableInTouchMode = false
        //半透明化StatusBar
        SystemBarUtil.transparentStatusBar(this)
        //隐藏StatusBar
        read_pv_page.post { SystemBarUtil.hideSystemBar(this,viewModel.getIsFullScreen()) }
        read_abl_top_menu.setPadding(0, ScreenUtil.getStatusBarHeight(), 0, 0)
        ll_download.setPadding(0, ScreenUtil.getStatusBarHeight(), 0, ScreenUtil.dpToPx(15))

        val lp = window.attributes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lp.layoutInDisplayCutoutMode = 1
        }
        window.attributes = lp

        //设置当前Activity的Brightness
        if (ReadSettingManager.getInstance().isBrightnessAuto) {
            BrightnessUtil.setDefaultBrightness(this)
        } else {
            BrightnessUtil.setBrightness(this, ReadSettingManager.getInstance().brightness)
        }

        //注册广播
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        intentFilter.addAction(Intent.ACTION_TIME_TICK)
        registerReceiver(mReceiver, intentFilter)

        if (!SpUtil.getBooleanValue(Constant.BookGuide, false)) {
            iv_guide.isVisible = true
            toggleMenu(false)
        }

        tv_book_name.text = viewModel.getCollBook().name
        mCategoryAdapter = CatalogueAdapter()
        rlv_list.adapter = mCategoryAdapter
        rlv_list.isFastScrollEnabled = true
        rlv_mark.layoutManager = LinearLayoutManager(this)

        mMarkAdapter = MarkAdapter()
        rlv_mark.adapter = mMarkAdapter
        toggleNightMode()
    }

    /**
     * 初始化监听
     */
    override fun initListener() {
        // 错误通知
        viewModel.toast.observe(this, Observer<String> {
            showToast(it)
        })

        // 获取阅读记录之后
        viewModel.getBookRecord.observe(this, Observer<ReadRecordBean> {
            var mBookRecord = it
            if (mBookRecord == null) {
                mBookRecord = ReadRecordBean()
            }
            mPageLoader.setBookRecord(mBookRecord)

            // 填充章节
            viewModel.fetchChapters(0, Int.MAX_VALUE)
        })

        // 获取章节之后
        viewModel.chapters.observe(this, Observer<ResponseChapter> {
            val chapterBeans = it.chapterBeans
            val cacheContents = it.cacheContents
            if (!cacheContents) {
                if (viewModel.getChapterStart() == 0) {
                    mPageLoader.getCollBook().chapters.clear()
                    mPageLoader.getCollBook().chapters.addAll(chapterBeans)
                } else {
                    mPageLoader.getCollBook().chapters.addAll(chapterBeans)
                }
                viewModel.moveBackChapterStart(chapterBeans.size)
                mPageLoader.refreshChapterList()
            } else {
                // 缓存章节内容
                if (!chapterBeans.isNullOrEmpty()) {
                    viewModel.fetchChapterContents(chapterBeans)
                }
            }
        })

        // 获取内容进行时
        viewModel.refreshChapter.observe(this, Observer<Int> {
            // 每下载完一个章节，就刷新那个章节下
            mCategoryAdapter.refreshItem(it)
        })

        // 获取章节内容之后
        viewModel.chapterContents.observe(this, Observer<ArrayList<ChapterBean>> {
            val pos = mPageLoader.getChapterPos()
            rlv_list.setSelection(pos)
            if (mPageLoader.getPageStatus() == PageLoader.STATUS_LOADING) {
                mPageLoader.openChapter()
            }
            // 当完成章节的时候，刷新列表
            mCategoryAdapter.notifyDataSetChanged()
        })

        // 获取内容错误
        viewModel.chapterContentsFetchingErr.observe(this, Observer<String> {
            if (mPageLoader.getPageStatus() == PageLoader.STATUS_LOADING) {
                mPageLoader.chapterError()
            }
        })

        // 添加书签之后
        viewModel.addSign.observe(this, Observer<MutableList<BookSignBean>> {
            mMarkAdapter.addItem(it.first())
        })

        // 获取书签之后
        viewModel.getSign.observe(this, Observer<List<BookSignBean>> {
            mMarkAdapter.refreshItems(it)
        })

        // 删除书签之后
        viewModel.deleteSign.observe(this, Observer<String> {
            viewModel.toastMsg(it)
        })
        // 保持阅读记录之后
        viewModel.saveBookRecord.observe(this, Observer<String> {
            showToast(it)
        })
        toolbar.setNavigationOnClickListener { finish() }
        read_setting_sb_brightness.progress = ReadSettingManager.getInstance().brightness
        rlv_list.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScroll(
                view: AbsListView?,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) {

            }

            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (view?.lastVisiblePosition == view?.count!! - 1) {
                        //加载更多章节
                        viewModel.fetchChapters(viewModel.getChapterStart())
                    }
                }
            }

        })

        mPageLoader.setOnPageChangeListener(
            object : OnPageChangeListener {
                override fun onChapterChange(pos: Int) {
                    var index = pos
                    val size = mCategoryAdapter.count
                    if (pos >= size) {
                        index = size - 1
                    }
                    mCategoryAdapter.setChapter(index)
                    viewModel.setCurrentChapter(index, mCategoryAdapter.getChapter(index))
                }

                override fun chapterContents(requestChapters: MutableList<ChapterBean>) {
                    viewModel.fetchChapterContents(requestChapters)
                }

                override fun onChaptersFinished(chapters: MutableList<ChapterBean>) {
                    mCategoryAdapter.refreshItems(chapters)
                }

                override fun onPageCountChange(count: Int) {}

                override fun onPageChange(pos: Int) {

                }
            }
        )
        read_pv_page.setTouchListener(object : OnTouchListener {
            override fun onTouch(): Boolean {
                return !hideReadMenu()
            }

            override fun center() {
                toggleMenu(true)
            }

            override fun prePage() {}

            override fun nextPage() {}

            override fun cancel() {}
        })
        read_tv_category.setOnClickListener {
            //移动到指定位置
            if (mCategoryAdapter.count > 0) {
                rlv_list.setSelection(mPageLoader.getChapterPos())
            }
            //切换菜单
            toggleMenu(true)
            //打开侧滑动栏
            read_dl_slide.openDrawer(GravityCompat.START)
        }
        tv_light.setOnClickListener {
            ll_light.isVisible = false
            rlReadMark.isVisible = false
            ll_light.isVisible = !ll_light.isVisible
        }
        tv_setting.setOnClickListener {
            ll_light.isVisible = false
            rlReadMark.isVisible = false
            toggleMenu(false)
            mSettingDialog.show()
        }
        read_setting_sb_brightness.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val progress = seekBar.progress
                //设置当前 Activity 的亮度
                BrightnessUtil.setBrightness(this@NovelReadActivity, progress)
                //存储亮度的进度条
                ReadSettingManager.getInstance().brightness = progress
            }
        })
        tvBookReadMode.setOnClickListener {
            mPageLoader.setNightMode(viewModel.negateIsNightMode())
            toggleNightMode()
        }
        read_tv_brief.setOnClickListener {
            //跳转到简介
            //            val intent = Intent(this, NovelBookDetailActivity::class.java)
//            intent.putExtra(Constant.Bundle.BookId, Integer.valueOf(mBookId))
//            startActivity(intent)
        }
        read_tv_community.setOnClickListener {
            if (read_ll_bottom_menu.isVisible) {
                if (rlReadMark.isVisible) {
                    rlReadMark.isVisible = false
                } else {
                    ll_light.isVisible = false
                    //获取书签
                    viewModel.fetchBookSign()
                    rlReadMark.isVisible = true
                }
            }
        }

        // 添加书签
        tvAddMark.setOnClickListener {
            mMarkAdapter.edit = false
            viewModel.addBookSign()
        }

        // 清除书签
        tvClear.setOnClickListener {
            if (mMarkAdapter.edit) {
                val sign = mMarkAdapter.selectList
                if (sign.isNotEmpty()) {
                    viewModel.deleteBookSign(sign)
                    mMarkAdapter.clear()
                }
                mMarkAdapter.edit = false
            } else {
                mMarkAdapter.edit = true
                mMarkAdapter.notifyDataSetChanged()
            }
        }
        // 书籍缓存
        tv_cache.setOnClickListener {
            val mCollBook = viewModel.getCollBook()
            if (mCollBook.favorite == 0) { //没有收藏 先收藏 然后弹框
                //设置为已收藏
                mCollBook.favorite = 1
                //设置阅读时间
                //mCollBook.lastRead = System.currentTimeMillis().toString()
            }
            showDownLoadDialog()
        }
        rlv_list.setOnItemClickListener { _, _, position, _ ->
            read_dl_slide.closeDrawer(GravityCompat.START)
            mPageLoader.skipToChapter(position)
        }
        iv_guide.setOnClickListener {
            iv_guide.isVisible = false
            SpUtil.setBooleanValue(Constant.BookGuide, true)
        }
    }

    /**
     * 初始化数据
     */
    override fun initData() {
        // 获取阅读记录
        viewModel.getBookRecord()
    }

    /**
     * 设置主题
     */
    private fun setTheme() {
        if (viewModel.compareNowMode()) {
            if (SpUtil.getBooleanValue(Constant.NIGHT)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            recreate()
        }
    }

    /**
     * 显示下载框
     */
    private fun showDownLoadDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.d_cache_num))
            .setItems(
                arrayOf(
                    getString(R.string.d_cache_last_50),
                    getString(R.string.d_cache_last_all),
                    getString(R.string.d_cache_all)
                )
            ) { _, which ->
                when (which) {
                    0 -> {
                        // 缓存后面50章 章节列表和内容
                        viewModel.fetchChapters(-1, 50, true)
                    }
                    1 -> {
                        // 缓存后面所有章节列表和内容
                        viewModel.fetchChapters( -1, Int.MAX_VALUE, true)
                    }
                    2 -> {
                        // 缓存所有章节列表和内容
                        viewModel.fetchChapters(0, Int.MAX_VALUE, true)
                    }
                }

                toggleMenu(true)
            }
        builder.show()
    }

    /**
     * 夜间模式
     */
    private fun toggleNightMode() {
        if (viewModel.getIsNightMode()) {
            tvBookReadMode.text = resources.getString(R.string.book_read_mode_day)
            val drawable = ContextCompat.getDrawable(this, R.drawable.ic_read_menu_moring)
            tvBookReadMode.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
            cl_layout.setBackgroundColor(ContextCompat.getColor(this, R.color.read_bg_night))
        } else {
            tvBookReadMode.text = resources.getString(R.string.book_read_mode_night)
            val drawable = ContextCompat.getDrawable(this, R.drawable.ic_read_menu_night)
            tvBookReadMode.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
            cl_layout.setBackgroundColor(
                    ContextCompat.getColor(
                            this,
                            ReadSettingManager.getInstance().pageStyle.bgColor
                    )
            )
        }
    }

    /**
     * 隐藏阅读界面的菜单显示
     *
     * @return 是否隐藏成功
     */
    private fun hideReadMenu(): Boolean {
        SystemBarUtil.hideSystemBar(this,viewModel.getIsFullScreen())
        if (read_abl_top_menu.isVisible) {
            toggleMenu(true)
            return true
        } else if (mSettingDialog.isShowing) {
            mSettingDialog.dismiss()
            return true
        }
        return false
    }

    /**
     * 切换菜单栏的可视状态
     * 默认是隐藏的
     */
    private fun toggleMenu(hideStatusBar: Boolean) {
        ll_light.isVisible = false
        rlReadMark.isVisible = false
        if (read_abl_top_menu.isVisible) {
            //关闭
            read_abl_top_menu.startAnimation(mTopOutAnim)
            read_ll_bottom_menu.startAnimation(mBottomOutAnim)
            read_abl_top_menu.isVisible = false
            read_ll_bottom_menu.isVisible = false

            if (hideStatusBar) {
                SystemBarUtil.hideSystemBar(this,viewModel.getIsFullScreen())
            }
        } else {
            read_abl_top_menu.isVisible = true
            read_ll_bottom_menu.isVisible = true
            read_abl_top_menu.startAnimation(mTopInAnim)
            read_ll_bottom_menu.startAnimation(mBottomInAnim)
            SystemBarUtil.showSystemBar(this,viewModel.getIsFullScreen())
        }
    }

    /**
     * 退出
     */
    private fun exit() {
        // 返回给BookDetail。
        val result = Intent()
        result.putExtra(RESULT_IS_COLLECTED, viewModel.getCollBook().favorite)
        setResult(Activity.RESULT_OK, result)
        finish()

    }

    /**
     * 静态内容
     */
    companion object {
        const val EXTRA_COLL_BOOK = "extra_coll_book"

        fun startFromActivity(activity: Activity, collBookBean: BookBean) {
            val intent = Intent(activity, NovelReadActivity::class.java)
            intent.putExtra(EXTRA_COLL_BOOK, collBookBean)
            activity.startActivity(intent)
        }

        fun startFromFragment(activity: FragmentActivity?, collBookBean: BookBean) {
            val intent = Intent(activity, NovelReadActivity::class.java)
            intent.putExtra(EXTRA_COLL_BOOK, collBookBean)
            activity?.startActivity(intent)
        }
    }
}

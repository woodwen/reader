package com.woodnoisu.reader.feature.reader.presentation.reader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.AbsListView
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.woodnoisu.reader.feature.reader.R
import com.woodnoisu.reader.feature.reader.domain.model.ReaderChapterDomainModel
import com.woodnoisu.reader.feature.reader.presentation.page.AnimManager
import com.woodnoisu.reader.feature.reader.presentation.page.PageLoader
import com.woodnoisu.reader.feature.reader.presentation.page.ReadSettingDialog
import com.woodnoisu.reader.feature.reader.presentation.page.ReadSettingManager
import com.woodnoisu.reader.feature.reader.presentation.page.event.OnPageChangeListener
import com.woodnoisu.reader.feature.reader.presentation.page.event.OnTouchListener
import com.woodnoisu.reader.feature.reader.presentation.reader.recyclerView.CatalogueAdapter
import com.woodnoisu.reader.feature.reader.presentation.reader.recyclerView.MarkAdapter
import com.woodnoisu.reader.library.base.model.*
import com.woodnoisu.reader.library.base.presentation.extension.observe
import com.woodnoisu.reader.library.base.presentation.fragment.InjectionFragment
import com.woodnoisu.reader.library.base.utils.*
import kotlinx.android.synthetic.main.fragment_reader.*
import kotlinx.android.synthetic.main.layout_download.*
import kotlinx.android.synthetic.main.layout_light.*
import kotlinx.android.synthetic.main.layout_read_mark.*
import org.kodein.di.generic.instance
import java.util.*

class ReaderFragment: InjectionFragment(R.layout.fragment_reader) {

    private val viewModel: ReaderViewModel by instance()
    private val mCategoryAdapter: CatalogueAdapter by instance()
    private val mMarkAdapter: MarkAdapter by instance()
    private val stateObserver = Observer<ViewState> {
        //refresh_layout.isRefreshing = it.isLoading
        showToast(it.errorMsg)

        val readerBookDomainModel = it.readerDomainModel.readerBookDomainModel
        val readerChapterDomainModels = it.readerDomainModel.readerChapterDomainModels
        val readerChapterContentDomainModels = it.readerDomainModel.readerChapterContentDomainModels
        val cacheContents = it.readerDomainModel.cacheContents
        val readerRecordDomainModel = it.readerDomainModel.readerRecordDomainModel
        val readerBookSignDomainModels = it.readerDomainModel.readerBookSignDomainModels
        val chapterContentsErr = it.readerDomainModel.chapterContentsErr
        // 设置阅读记录
        if (readerRecordDomainModel != null) {
            // 设置阅读记录
            mPageLoader.setBookRecord(readerRecordDomainModel)
            // 填充章节
            viewModel.getChapters(viewModel.getCollBook(), 0, Int.MAX_VALUE)
        }
        // 设置章节
        if (readerChapterDomainModels != null) {
            if (!cacheContents) {
                if (viewModel.getChapterStart() == 0) {
                    mPageLoader.getCollBook().chapters.clear()
                    mPageLoader.getCollBook().chapters.addAll(readerChapterDomainModels)
                } else {
                    mPageLoader.getCollBook().chapters.addAll(readerChapterDomainModels)
                }
                viewModel.moveBackChapterStart(readerChapterDomainModels.size)
                mPageLoader.refreshChapterList()
            } else {
                // 缓存章节内容
                if (!readerChapterDomainModels.isNullOrEmpty()) {
                    viewModel.getChapterContents(readerChapterDomainModels)
                }
            }
        }
        // 设置章节内容
        if (readerChapterContentDomainModels != null) {
            val pos = mPageLoader.getChapterPos()
            rlv_list.setSelection(pos)
            if (mPageLoader.getPageStatus() == PageLoader.STATUS_LOADING) {
                mPageLoader.openChapter()
            }
            // 当完成章节的时候，刷新列表
            mCategoryAdapter.notifyDataSetChanged()
        }
        // 章节内容加载错误
        if (chapterContentsErr) {
            if (mPageLoader.getPageStatus() == PageLoader.STATUS_LOADING) {
                mPageLoader.chapterError()
            }
        }
        // 设置书籍
        if (readerBookDomainModel != null) {
            viewModel.navigateShow(false)
            viewModel.setCollBook(readerBookDomainModel)
            tv_book_name.text = readerBookDomainModel.name
            initFragment()
        }
        // 设置书签
        if (readerBookSignDomainModels != null) {
            mMarkAdapter.refreshItems(readerBookSignDomainModels)
        }
    }
    // 动画管理器
    private lateinit var animManager:AnimManager
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
     * 退出之前
     */
    override fun onPause() {
        exit()
        super.onPause()
    }

    /**
     * 初始化页面
     */
    override fun initView() {
        //保持屏幕亮
        SystemUtil.screenAlwaysOn(requireActivity())

        //半透明化StatusBar
        SystemBarUtil.transparentStatusBar(requireActivity())

        // 设置主题
        if (viewModel.compareNowMode()) {
            if (SpUtil.getBooleanValue(Constant.NIGHT)) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            requireActivity().recreate()
        }

        // 初始化动画
        animManager = AnimManager(requireContext())

        //隐藏StatusBar
        read_pv_page.post { SystemBarUtil.hideSystemBar(requireActivity(),viewModel.getIsFullScreen()) }
        // 设置上部工具栏
        read_abl_top_menu.setPadding(0, ScreenUtil.getStatusBarHeight(), 0, 0)
        // 设置下部工具栏
        ll_download.setPadding(0, ScreenUtil.getStatusBarHeight(), 0, ScreenUtil.dpToPx(15))

        // 显示类型
        val lp = requireActivity().window.attributes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lp.layoutInDisplayCutoutMode = 1
        }
        requireActivity().window.attributes = lp

        //设置当前Activity的自动调节亮度
        if (ReadSettingManager.getInstance().isBrightnessAuto) {
            BrightnessUtil.setDefaultBrightness(requireActivity())
        } else {
            BrightnessUtil.setBrightness(requireActivity(), ReadSettingManager.getInstance().brightness)
        }
        read_setting_sb_brightness.progress = ReadSettingManager.getInstance().brightness

        // 简体/繁体
        if (!SpUtil.getBooleanValue(Constant.BookGuide, false)) {
            iv_guide.isVisible = true
            toggleMenu(false)
        }

        read_dl_slide.apply {
            //禁止滑动展示DrawerLayout
            setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            //侧边打开后，返回键能够起作用
            isFocusableInTouchMode = false
        }

        // 初始化章节列表
        rlv_list.apply {
            adapter = mCategoryAdapter
            isFastScrollEnabled = true
        }

        // 初始化书签
        rlv_mark.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mMarkAdapter
        }

        // 设置夜间模式
        toggleNightMode()

        // 退出事件
        toolbar.setNavigationOnClickListener {
            exit()
            viewModel.navigateToBack()
        }

        // 章节列表滚动事件
        rlv_list.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScroll(
                    view: AbsListView?,
                    firstVisibleItem: Int,
                    visibleItemCount: Int,
                    totalItemCount: Int) {}

            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (view?.lastVisiblePosition == view?.count!! - 1) {
                        //加载更多章节
                        viewModel.getChapters(viewModel.getCollBook(),viewModel.getChapterStart())
                    }
                }
            }
        })


        // 阅读页面点击事件
        read_pv_page.setTouchListener(object : OnTouchListener {
            override fun onTouch() = !hideReadMenu()
            override fun center() = toggleMenu(true)
            override fun prePage() {}
            override fun nextPage() {}
            override fun cancel() {}
        })

        // 展示章节列表点击事件
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

        // 亮度按钮点击事件
        tv_light.setOnClickListener {
            ll_light.isVisible = false
            rlReadMark.isVisible = false
            ll_light.isVisible = !ll_light.isVisible
        }

        // 设置按钮点击事件
        tv_setting.setOnClickListener {
            ll_light.isVisible = false
            rlReadMark.isVisible = false
            toggleMenu(false)
            mSettingDialog.show()
        }

        // 亮度调节事件
        read_setting_sb_brightness.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val progress = seekBar.progress
                //设置当前 Activity 的亮度
                BrightnessUtil.setBrightness(requireActivity(), progress)
                //存储亮度的进度条
                ReadSettingManager.getInstance().brightness = progress
            }
        })

        // 夜间/日间阅读模式点击事件
        tvBookReadMode.setOnClickListener {
            mPageLoader.setNightMode(viewModel.negateIsNightMode())
            toggleNightMode()
        }

        // 跳转到简介点击事件
        read_tv_brief.setOnClickListener {
            //跳转到简介
            //            val intent = Intent(this, NovelBookDetailActivity::class.java)
//            intent.putExtra(Constant.Bundle.BookId, Integer.valueOf(mBookId))
//            startActivity(intent)
        }

        //  书签点击事件
        read_tv_community.setOnClickListener {
            if (read_ll_bottom_menu.isVisible) {
                if (rlReadMark.isVisible) {
                    rlReadMark.isVisible = false
                } else {
                    ll_light.isVisible = false
                    //获取书签
                    viewModel.getSigns()
                    rlReadMark.isVisible = true
                }
            }
        }

        // 添加书签
        tvAddMark.setOnClickListener {
            mMarkAdapter.edit = false
            viewModel.addSign()
        }

        // 清除书签
        tvClear.setOnClickListener {
            if (mMarkAdapter.edit) {
                val sign = mMarkAdapter.selectList
                if (sign.isNotEmpty()) {
                    viewModel.deleteSigns(sign)
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

        // 章节列表点击事件
        rlv_list.setOnItemClickListener { _, _, position, _ ->
            read_dl_slide.closeDrawer(GravityCompat.START)
            mPageLoader.skipToChapter(position)
        }


        iv_guide.setOnClickListener {
            iv_guide.isVisible = false
            SpUtil.setBooleanValue(Constant.BookGuide, true)
        }

        // 监听事件
        observe(viewModel.stateLiveData, stateObserver)
        viewModel.loadData()
    }

    /**
     * 加载书籍完成后执行
     */
    private fun initFragment(){
        //获取页面加载器
        mPageLoader = read_pv_page.getPageLoader(viewModel.getCollBook())

        // 阅读设置器
        mSettingDialog = ReadSettingDialog(requireActivity(), mPageLoader)

        //注册广播
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        intentFilter.addAction(Intent.ACTION_TIME_TICK)
        requireActivity().registerReceiver(mReceiver, intentFilter)

        // 翻页事件
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
                    override fun chapterContents(requestChapters: MutableList<ReaderChapterDomainModel>) {
                        viewModel.getChapterContents(requestChapters)
                    }
                    override fun onChaptersFinished(chapters: MutableList<ReaderChapterDomainModel>) {
                        mCategoryAdapter.refreshItems(chapters)
                    }
                    override fun onPageCountChange(count: Int) {}
                    override fun onPageChange(pos: Int) {}
                }
        )

        // 获取阅读记录
        viewModel.getBookRecord()
    }

    /**
     * 显示下载框
     */
    private fun showDownLoadDialog() {
        val builder = AlertDialog.Builder(requireContext())
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
                        viewModel.getChapters(viewModel.getCollBook(),-1, 50, true)
                    }
                    1 -> {
                        // 缓存后面所有章节列表和内容
                        viewModel.getChapters(viewModel.getCollBook(), -1, Int.MAX_VALUE, true)
                    }
                    2 -> {
                        // 缓存所有章节列表和内容
                        viewModel.getChapters(viewModel.getCollBook(),0, Int.MAX_VALUE, true)
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
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_read_menu_moring)
            tvBookReadMode.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
            cl_layout.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.read_bg_night))
        } else {
            tvBookReadMode.text = resources.getString(R.string.book_read_mode_night)
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_read_menu_night)
            tvBookReadMode.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null)
            cl_layout.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
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
        SystemBarUtil.hideSystemBar(requireActivity(),viewModel.getIsFullScreen())
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
            read_abl_top_menu.startAnimation(animManager.mTopOutAnim)
            read_ll_bottom_menu.startAnimation(animManager.mBottomOutAnim)
            read_abl_top_menu.isVisible = false
            read_ll_bottom_menu.isVisible = false

            if (hideStatusBar) {
                SystemBarUtil.hideSystemBar(requireActivity(),viewModel.getIsFullScreen())
            }
        } else {
            read_abl_top_menu.isVisible = true
            read_ll_bottom_menu.isVisible = true
            read_abl_top_menu.startAnimation(animManager.mTopInAnim)
            read_ll_bottom_menu.startAnimation(animManager.mBottomInAnim)
            SystemBarUtil.showSystemBar(requireActivity(),viewModel.getIsFullScreen())
        }
    }

    /**
     * 退出
     */
    private fun exit() {
        //存储阅读记录
        viewModel.saveBookRecord(mPageLoader.getRecord())
        viewModel.navigateShow(true)
        mSettingDialog.dismiss()
        mPageLoader.closeBook()
        try {
            requireActivity().unregisterReceiver(mReceiver)
        }catch(e:Throwable){
            e.printStackTrace()
        }
    }
}

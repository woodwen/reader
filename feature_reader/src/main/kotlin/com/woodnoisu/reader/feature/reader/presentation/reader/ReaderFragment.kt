package com.woodnoisu.reader.feature.reader.presentation.reader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.view.WindowManager
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
import com.pawegio.kandroid.visible
import com.woodnoisu.reader.feature.reader.R
import com.woodnoisu.reader.feature.reader.domain.model.ReaderChapterDomainModel
import com.woodnoisu.reader.feature.reader.domain.model.ReaderRecordDomainModel
import com.woodnoisu.reader.feature.reader.presentation.page.AnimManager
import com.woodnoisu.reader.feature.reader.presentation.page.PageLoader
import com.woodnoisu.reader.feature.reader.presentation.page.ReadSettingDialog
import com.woodnoisu.reader.feature.reader.presentation.page.ReadSettingManager
import com.woodnoisu.reader.feature.reader.presentation.page.event.OnPageChangeListener
import com.woodnoisu.reader.feature.reader.presentation.page.event.OnTouchListener
import com.woodnoisu.reader.feature.reader.presentation.reader.recyclerView.CatalogueAdapter
import com.woodnoisu.reader.feature.reader.presentation.reader.recyclerView.MarkAdapter
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

    // 动画管理器
    private lateinit var animManager: AnimManager

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
     * 销毁
     */
    override fun onDestroy() {
        super.onDestroy()
        mSettingDialog.dismiss()
        mPageLoader.closeBook()
        requireActivity().unregisterReceiver(mReceiver)
    }

    /**
     * 初始化窗口
     */
    override fun initView() {
        initConfig()

        initFragmentViewModelListener()

        rlv_list.adapter = mCategoryAdapter
        rlv_list.isFastScrollEnabled = true

        rlv_mark.layoutManager = LinearLayoutManager(requireContext())
        rlv_mark.adapter = mMarkAdapter

        viewModel.navigateShow(false)
        viewModel.loadData()
    }

    /**
     * 初始化系统配置
     */
    private fun initConfig(){
        // 初始化动画
        animManager = AnimManager(requireContext())

        // 设置状态栏风格
        //StatusBarUtil.setBarsStyle(requireActivity(), R.color.colorPrimary, true)


        //禁止滑动展示DrawerLayout
        read_dl_slide.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        //侧边打开后，返回键能够起作用
        read_dl_slide.isFocusableInTouchMode = false

        //半透明化StatusBar
        SystemBarUtil.transparentStatusBar(requireActivity())

        //隐藏StatusBar
        SystemBarUtil.hideSystemBar(
            requireActivity(),
            ReadSettingManager.getInstance().isFullScreen
        )

        // 设置顶部工具栏
        read_abl_top_menu.setPadding(0, ScreenUtil.getStatusBarHeight(), 0, 0)

        //保持屏幕亮
        SystemUtil.screenAlwaysOn(requireActivity())

        // 自定义显示模式
        SystemUtil.layoutInDisplayCutoutMode(requireActivity())

        // 设置亮度
        if (ReadSettingManager.getInstance().isBrightnessAuto) {
            BrightnessUtil.setDefaultBrightness(requireActivity())
        } else {
            BrightnessUtil.setBrightness(
                requireActivity(),
                ReadSettingManager.getInstance().brightness
            )
        }

        // 设置日/夜阅读模式
        toggleNightMode()
    }

    /**
     * 初始化viewModel监听
     */
    private fun initFragmentViewModelListener() {
        //  获取书籍之后
        viewModel.bookLiveData.observe(this, Observer {
            val book = it.book
            tv_book_name.text = book.name
            // 初始化书籍
            viewModel.setBook(book)
            initFragmentView()
            initFragmentListener()
            initFragmentData()
        })

        // 错误通知
        viewModel.toast.observe(this, Observer {
            showToast(it)
        })

        // 获取阅读记录之后
        viewModel.bookRecordLiveData.observe(this, Observer {
            var mBookRecord = it.record
            if (mBookRecord == null) {
                mBookRecord = ReaderRecordDomainModel()
            }
            mPageLoader.setBookRecord(mBookRecord)

            // 填充章节
            viewModel.fetchChapters(0, Int.MAX_VALUE)
        })

        // 获取章节之后
        viewModel.chaptersLiveData.observe(this, Observer {
            val chapterBeans = it.chapters
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
        viewModel.refreshChapterLiveData.observe(this, Observer {
            // 每下载完一个章节，就刷新那个章节下
            mCategoryAdapter.refreshItem(it)
        })

        // 获取章节内容之后
        viewModel.chapterContentsLiveData.observe(this, Observer {
            val pos = mPageLoader.getChapterPos()
            rlv_list.setSelection(pos)
            if (mPageLoader.getPageStatus() == PageLoader.STATUS_LOADING) {
                mPageLoader.openChapter()
            }
            // 当完成章节的时候，刷新列表
            mCategoryAdapter.notifyDataSetChanged()
        })

        // 获取内容错误
        viewModel.chapterContentErrLiveData.observe(this, Observer {
            if (mPageLoader.getPageStatus() == PageLoader.STATUS_LOADING) {
                mPageLoader.chapterError()
            }
        })

        // 添加书签之后
        viewModel.addSignLiveData.observe(this, Observer {
            val sign = it.sign
            mMarkAdapter.addItem(sign)
        })

        // 获取书签之后
        viewModel.signsLiveData.observe(this, Observer {
            val signs = it.signs
            mMarkAdapter.refreshItems(signs)
        })

        // 删除书签之后
        viewModel.deleteSignsLiveData.observe(this, Observer {
            viewModel.toastMsg("删除书签成功")
        })

        // 保存阅读记录之后
        viewModel.saveBookRecordLiveData.observe(this, Observer {
            // viewModel.toastMsg("保存阅读记录成功")
            SystemBarUtil.showSystemBar(
                requireActivity(),
                ReadSettingManager.getInstance().isFullScreen
            )
            viewModel.navigateShow(true)
            viewModel.navigateToBack()
        })
    }

    /**
     * 当获取到书籍后初始化View
     */
    private fun initFragmentView() {
        //获取页面加载器
        mPageLoader = read_pv_page.getPageLoader(viewModel.getBook())

        // 阅读设置器
        mSettingDialog = ReadSettingDialog(requireActivity(), mPageLoader)

        //注册广播
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        intentFilter.addAction(Intent.ACTION_TIME_TICK)
        requireActivity().registerReceiver(mReceiver, intentFilter)
    }

    /**
     * 初始化监听
     */
    private fun initFragmentListener() {
        // 页面变化事件
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
                    viewModel.fetchChapterContents(requestChapters)
                }

                override fun onChaptersFinished(chapters: MutableList<ReaderChapterDomainModel>) {
                    mCategoryAdapter.refreshItems(chapters)
                }

                override fun onPageCountChange(count: Int) {}

                override fun onPageChange(pos: Int) {}
            }
        )

        // 返回事件
        toolbar.setNavigationOnClickListener {
            //存储阅读记录
            viewModel.fetchSaveBookRecord(mPageLoader.getRecord())
        }

        // 章节列表滚动事件
        rlv_list.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScroll(
                view: AbsListView?,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) {}

            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (view?.lastVisiblePosition == view?.count!! - 1) {
                        //加载更多章节
                        viewModel.fetchChapters(viewModel.getChapterStart())
                    }
                }
            }

        })

        // 章节列表项目点击事件
        rlv_list.setOnItemClickListener { _, _, position, _ ->
            read_dl_slide.closeDrawer(GravityCompat.START)
            mPageLoader.skipToChapter(position)
        }

        // 阅读页中间触摸事件
        read_pv_page.setTouchListener(object : OnTouchListener {
            override fun onTouch(): Boolean {
                return !hideReadMenu()
            }

            override fun center() {
                toggleMenu()
            }

            override fun prePage() {}
            override fun nextPage() {}
            override fun cancel() {}
        })

        // 显示章节列表点击事件
        read_tv_category.setOnClickListener {
            //移动到指定位置
            if (mCategoryAdapter.count > 0) {
                rlv_list.setSelection(mPageLoader.getChapterPos())
            }
            //切换菜单
            toggleMenu()
            //打开侧滑动栏
            read_dl_slide.openDrawer(GravityCompat.START)
        }

        // 亮度点击事件
        tv_light.setOnClickListener {
            ll_light.isVisible = false
            rlReadMark.isVisible = false
            ll_light.isVisible = !ll_light.isVisible
        }

        // 点击下载事件
        tv_cache.setOnClickListener {
            val mCollBook = viewModel.getBook()
            if (mCollBook.favorite == 0) { //没有收藏 先收藏 然后弹框
                //设置为已收藏
                mCollBook.favorite = 1
                //设置阅读时间
                //mCollBook.lastRead = System.currentTimeMillis().toString()
            }
            // 弹出下载框
            DialogUtil.showDialog(
                activity = requireActivity(),
                title = "缓存章节",
                list = arrayOf(getString(R.string.d_cache_last_50),
                    getString(R.string.d_cache_last_all),
                    getString(R.string.d_cache_all)).toList(),
                onListItemClick = { index,_->
                    when (index) {
                        0 -> {
                            // 缓存后面50章 章节列表和内容
                            viewModel.fetchChapters(-1, 50, true)
                        }
                        1 -> {
                            // 缓存后面所有章节列表和内容
                            viewModel.fetchChapters(-1, Int.MAX_VALUE, true)
                        }
                        2 -> {
                            // 缓存所有章节列表和内容
                            viewModel.fetchChapters(0, Int.MAX_VALUE, true)
                        }
                    }

                    toggleMenu()
                }
            )
        }

        // 设置点击事件
        tv_setting.setOnClickListener {
            ll_light.isVisible = false
            rlReadMark.isVisible = false
            toggleMenu()
            mSettingDialog.show()
        }

        // 阅读亮度调节事件
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

        // 日/夜模式切换事件
        tvBookReadMode.setOnClickListener {
            ReadSettingManager.getInstance().isNightMode = !ReadSettingManager.getInstance().isNightMode
            mPageLoader.setNightMode(ReadSettingManager.getInstance().isNightMode)
            toggleNightMode()
        }

        // 点击简介事件
        read_tv_brief.setOnClickListener {
            //跳转到简介
            //            val intent = Intent(this, NovelBookDetailActivity::class.java)
//            intent.putExtra(Constant.Bundle.BookId, Integer.valueOf(mBookId))
//            startActivity(intent)
        }

        // 点击显示书签事件
        read_tv_community.setOnClickListener {
            if (read_ll_bottom_menu.isVisible) {
                if (rlReadMark.isVisible) {
                    rlReadMark.isVisible = false
                } else {
                    ll_light.isVisible = false
                    //获取书签
                    viewModel.fetchBookSigns()
                    rlReadMark.isVisible = true
                }
            }
        }

        // 添加书签
        tvAddMark.setOnClickListener {
            mMarkAdapter.edit = false
            viewModel.fetchAddSign()
        }

        // 清除书签
        tvClear.setOnClickListener {
            if (mMarkAdapter.edit) {
                val sign = mMarkAdapter.selectList
                if (sign.isNotEmpty()) {
                    viewModel.fetchDeleteSigns(sign)
                    mMarkAdapter.clear()
                }
                mMarkAdapter.edit = false
            } else {
                mMarkAdapter.edit = true
                mMarkAdapter.notifyDataSetChanged()
            }
        }
    }

    /**
     * 初始化数据
     */
    private fun initFragmentData() {
        // 设置亮度值
        read_setting_sb_brightness.progress = ReadSettingManager.getInstance().brightness
        // 获取阅读记录
        viewModel.fetchBookRecord()
    }

    /**
     * 日/夜模式
     */
    private fun toggleNightMode() {
        if (ReadSettingManager.getInstance().isNightMode) {
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
     */
    private fun hideReadMenu(): Boolean {
        if (read_abl_top_menu.isVisible) {
            toggleMenu()
            return true
        } else if (mSettingDialog.isShowing) {
            mSettingDialog.dismiss()
            return true
        }
        return false
    }

    /**
     * 切换菜单栏的可视状态（默认是隐藏的）
     */
    private fun toggleMenu() {
        ll_light.isVisible = false
        rlReadMark.isVisible = false
        if (read_abl_top_menu.isVisible) {
            //关闭
            read_abl_top_menu.startAnimation(animManager.mTopOutAnim)
            read_ll_bottom_menu.startAnimation(animManager.mBottomOutAnim)
            read_abl_top_menu.isVisible = false
            read_ll_bottom_menu.isVisible = false
        } else {
            read_abl_top_menu.isVisible = true
            read_ll_bottom_menu.isVisible = true
            read_abl_top_menu.startAnimation(animManager.mTopInAnim)
            read_ll_bottom_menu.startAnimation(animManager.mBottomInAnim)
        }
    }
}

package com.woodnoisu.reader.ui.shelf

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.woodnoisu.reader.R
import com.woodnoisu.reader.base.BaseFragment
import com.woodnoisu.reader.model.BookBean
import com.woodnoisu.reader.ui.novelRead.NovelReadActivity
import com.woodnoisu.reader.utils.FileUtil
import com.woodnoisu.reader.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_shelf.*
import kotlinx.android.synthetic.main.fragment_shelf.refresh_layout
import javax.inject.Inject

@AndroidEntryPoint
class ShelfFragment: BaseFragment() {
    @Inject
    lateinit var viewModelFactory: ShelfViewModel.AssistedFactory

    @VisibleForTesting
    val viewModel: ShelfViewModel by viewModels {
        ShelfViewModel.provideFactory(viewModelFactory)
    }

    // 书架适配
    private lateinit var adapter: ShelfAdapter

    /**
     * 获取界面id
     */
    override fun getRLayout():Int=R.layout.fragment_shelf

    /**
     * 初始化界面
     */
    override fun initView(){
        // 初始化刷新颜色
        refresh_layout.setColorSchemeResources(R.color.colorAccent)

        // 初始化书架适配器
        adapter = ShelfAdapter()

        // 初始化管理器
        rv_shelf.layoutManager = GridLayoutManager(activity, 3)
        rv_shelf.adapter = adapter
    }

    /**
     * 初始化监听
     */
    override fun initListener(){
        // 跳转页面事件
        val startActivityLaunch = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            val uri = it
            if (context != null && uri != null) {
                val bookModel = BookBean()
                val name = FileUtil.uriToName(uri, activity as Context)
                //需要加入修改
                val path = FileUtil.getFilePathForN(uri, activity as Context)
                //val path = uri.path?.split("raw:")?.get(1)
                bookModel.name = name
                bookModel.bookFilePath = path!!
                viewModel.insertBook(bookModel)
            }
        }

        // 书籍适配器 项目点击事件
        adapter.itemPositionClickListener = object : ShelfAdapter.OnItemPositionClickListener {
            /**
             * 打开项目
             */
            override fun openItem(position: Int, t: BookBean) {
//                if (t.url.isNullOrBlank()) {
//                    //本地阅读
//                    t.isLocal = 1
//                }
                NovelReadActivity.startFromFragment(activity, t)
            }

            /**
             * 删除项目
             */
            override fun deleteItem(position: Int, t: BookBean) {
                viewModel.deleteBook(t)
            }
        }

        // 设置刷新事件
        refresh_layout.setOnRefreshListener {
            // 刷新数据
            viewModel.fetchBookList("")
        }

        // 设置搜索框内容变更事件
        et_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.fetchBookList(s.toString().trim())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        //点击软键盘外部，收起软键盘
        et_search.setOnFocusChangeListener{ view, hasFocus ->
            if (!hasFocus) {
                val manager =
                    context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager?.hideSoftInputFromWindow(
                    view.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        }

        // 更多点击事件
        iv_title_more.setOnClickListener { v ->
            // 声明弹出框
            val popupWindow = PopupMenu(activity, iv_title_more)
            // 初始化按钮
            popupWindow.inflate(R.menu.shelf_pop_menu)
            // 设置按钮事件
            popupWindow.setOnMenuItemClickListener {
                when (it.itemId) {
                    //管理书架
                    R.id.shelf_manage -> {
                        //显示完成按钮
                        tv_complete.visibility = View.VISIBLE
                        //设置管理模式
                        adapter.edit = true
                    }
                    //添加本地书籍
                    R.id.shelf_add -> {
                        startActivityLaunch.launch(arrayOf("text/*", "text/plain"))
                    }
                }
                return@setOnMenuItemClickListener true
            }
            popupWindow.show()
        }

        // 完成点击事件
        tv_complete.setOnClickListener {
            //隐藏完成按钮
            tv_complete.isVisible = false
            //关闭管理模式
            adapter.edit = false
        }

        //错误通知事件
        viewModel.toast.observe(this, Observer<String> {
            showToast(it)
        })

        //是否显示加载框
        viewModel.isLoading.observe(this, Observer<Boolean> {
            refresh_layout.isRefreshing = it
        })

        //全部刷新
        viewModel.bookList.observe(viewLifecycleOwner, Observer<List<BookBean>> {
            adapter.refreshItems(it)
            refresh_layout.isRefreshing = false
        })

        //新增书籍
        viewModel.bookInserted.observe(viewLifecycleOwner, Observer<BookBean>{
            adapter.addItem(it)
        })

        //删除书籍
        viewModel.bookDeleted.observe(viewLifecycleOwner, Observer<BookBean>{
            adapter.removeItem(it)
        })
    }

    /**
     * 初始化数据
     */
    override fun initData(){
        //填充默认数据
        viewModel.fetchBookList("")
    }
}

package com.woodnoisu.reader.ui.shelf

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.woodnoisu.reader.R
import com.woodnoisu.reader.base.BaseFragment
import com.woodnoisu.reader.databinding.FragmentShelfBinding
import com.woodnoisu.reader.model.BookBean
import com.woodnoisu.reader.ui.novelRead.NovelReadActivity
import com.woodnoisu.reader.ui.square.SquareAdapter
import com.woodnoisu.reader.utils.FileUtil
import com.woodnoisu.reader.utils.showToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShelfFragment: BaseFragment() {
    private var _binding: FragmentShelfBinding? = null
    private val binding get() = _binding!!

    @VisibleForTesting
    val viewModel: ShelfViewModel by viewModels()

    // 书架适配
    private lateinit var adapter: ShelfAdapter
    private lateinit var sourceAdapter: SquareAdapter
    private var showingRemote = false

    /**
     * 获取界面id
     */
    override fun getRLayout():Int=R.layout.fragment_shelf

    /**
     * 初始化界面
     */
    override fun initView(){
        _binding = FragmentShelfBinding.bind(requireView())
        // 初始化刷新颜色
        binding.refreshLayout.setColorSchemeResources(R.color.colorAccent)

        // 初始化书架适配器
        adapter = ShelfAdapter()
        sourceAdapter = SquareAdapter()

        // 初始化管理器
        binding.rvShelf.layoutManager = GridLayoutManager(activity, 3)
        binding.rvShelf.adapter = adapter
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
        sourceAdapter.itemClickListener = object : SquareAdapter.OnBookItemClickListener {
            override fun openItem(t: BookBean) {
                viewModel.fetchBookInfo(t)
            }
        }

        // 设置刷新事件
        binding.refreshLayout.setOnRefreshListener {
            if (showingRemote && viewModel.refreshRemoteSearch()) {
                return@setOnRefreshListener
            }
            viewModel.fetchBookList("")
        }

        // 设置搜索框内容变更事件
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                showLocalMode()
                viewModel.fetchBookList(s.toString().trim())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.tvSourceSearch.setOnClickListener {
            searchAllSources()
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchAllSources()
                true
            } else {
                false
            }
        }

        //点击软键盘外部，收起软键盘
        binding.etSearch.setOnFocusChangeListener{ view, hasFocus ->
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
        binding.ivTitleMore.setOnClickListener { v ->
            // 声明弹出框
            val popupWindow = PopupMenu(activity, binding.ivTitleMore)
            // 初始化按钮
            popupWindow.inflate(R.menu.shelf_pop_menu)
            // 设置按钮事件
            popupWindow.setOnMenuItemClickListener {
                when (it.itemId) {
                    //管理书架
                    R.id.shelf_manage -> {
                        showLocalMode()
                        //显示完成按钮
                        binding.tvComplete.visibility = View.VISIBLE
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
        binding.tvComplete.setOnClickListener {
            //隐藏完成按钮
            binding.tvComplete.isVisible = false
            //关闭管理模式
            adapter.edit = false
        }

        //错误通知事件
        viewModel.toast.observe(this, Observer<String> {
            showToast(it)
        })

        //是否显示加载框
        viewModel.isLoading.observe(this, Observer<Boolean> {
            binding.refreshLayout.isRefreshing = it
        })

        //全部刷新
        viewModel.bookList.observe(viewLifecycleOwner, Observer<List<BookBean>> {
            adapter.refreshItems(it)
            binding.refreshLayout.isRefreshing = false
        })

        //新增书籍
        viewModel.bookInserted.observe(viewLifecycleOwner, Observer<BookBean>{
            adapter.addItem(it)
        })

        //删除书籍
        viewModel.bookDeleted.observe(viewLifecycleOwner, Observer<BookBean>{
            adapter.removeItem(it)
        })

        viewModel.remoteSearch.observe(viewLifecycleOwner, Observer {
            showRemoteMode()
            if (it.currentPage == 1) {
                sourceAdapter.refreshItems(it.bookBeans)
            } else {
                sourceAdapter.addItems(it.bookBeans)
            }
            binding.refreshLayout.isRefreshing = false
        })

        viewModel.bookInfo.observe(viewLifecycleOwner, Observer {
            if (it?.bookBean != null) {
                val bookBean = it.bookBean
                viewModel.fetchBook(bookBean)
                MaterialDialog(requireContext()).show {
                    title(text = bookBean.name)
                    message(text = "作者：${bookBean.author}\n类别：${bookBean.category}\n状态：${bookBean.status}\n简介：${bookBean.desc}")
                    positiveButton(text = "开始阅读") {
                        val selectedBook = viewModel.getBookBean()
                        if (selectedBook != null) {
                            NovelReadActivity.startFromFragment(activity, selectedBook)
                        } else {
                            showToast("打开书籍异常，请重新获取书籍")
                        }
                    }
                    negativeButton(text = "加入书架") {
                        viewModel.insertBook(bookBean)
                    }
                    lifecycleOwner(requireActivity())
                }
            }
        })
    }

    /**
     * 初始化数据
     */
    override fun initData(){
        //填充默认数据
        viewModel.fetchBookList("")
    }

    private fun searchAllSources() {
        val keyword = binding.etSearch.text?.toString()?.trim().orEmpty()
        if (keyword.isBlank()) {
            showToast("关键字不能为空")
            return
        }
        viewModel.fetchRemoteSearch(keyword)
    }

    private fun showRemoteMode() {
        if (showingRemote) return
        showingRemote = true
        binding.tvComplete.isVisible = false
        adapter.edit = false
        binding.rvShelf.layoutManager = LinearLayoutManager(activity)
        binding.rvShelf.adapter = sourceAdapter
    }

    private fun showLocalMode() {
        if (!showingRemote) return
        showingRemote = false
        sourceAdapter.clear()
        binding.rvShelf.layoutManager = GridLayoutManager(activity, 3)
        binding.rvShelf.adapter = adapter
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

package com.woodnoisu.reader.ui.square

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.listItems
import com.woodnoisu.reader.R
import com.woodnoisu.reader.base.BaseFragment
import com.woodnoisu.reader.databinding.FragmentSquareBinding
import com.woodnoisu.reader.model.*
import com.woodnoisu.reader.ui.novelRead.NovelReadActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * 广场页
 */
@AndroidEntryPoint
class SquareFragment: BaseFragment() {
    private var _binding: FragmentSquareBinding? = null
    private val binding get() = _binding!!

    //书籍列表适配器
    private lateinit var squareAdapter: SquareAdapter

    @VisibleForTesting
    val viewModel: SquareViewModel by viewModels()

    /**
     * 获取界面id
     */
    override fun getRLayout(): Int = R.layout.fragment_square

    /**
     * 初始化界面
     */
    override fun initView() {
        _binding = FragmentSquareBinding.bind(requireView())
        // 刷新框架主题色
        binding.refreshLayout.setColorSchemeResources(R.color.colorAccent)
        showSquareMessage(null)

        //初始化列表适配器
        squareAdapter = SquareAdapter()

        // 初始化主显示界面
        binding.rvTypes.apply {
            adapter = squareAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    /**
     * 初始化监听
     */
    @SuppressLint("WrongConstant")
    override fun initListener() {
        // 小说书城弹出框事件
        binding.searchTitle.tvSearchTitle.setOnClickListener {
            MaterialDialog(requireContext()).show {
                title(text = "书城分类")
                val options = viewModel.getSourceOptionsSnapshot()
                listItems(items = options.map { it.name }) { _, index, _ ->
                    val option = options[index]
                    viewModel.fetchShopOption(option)
                    if (option.dynamic) {
                        binding.searchTitle.tvSearchTitle.text = viewModel.getShopTitle()
                        if (option.canExplore) {
                            searchData(typeName = viewModel.getTypes()[0])
                        } else {
                            squareAdapter.clear()
                            binding.searchTitle.tvSearchFilter.text = "仅搜索"
                            showSquareMessage("动态书源请先输入书名或作者搜索")
                        }
                    } else {
                        val typeName = viewModel.getTypes()[0]
                        searchData(typeName = typeName)
                    }
                }
                lifecycleOwner(requireActivity())
            }
        }

        // 小说分类弹出框事件
        binding.searchTitle.tvSearchFilter.setOnClickListener {
            if (viewModel.isDynamicSource()) {
                if (viewModel.canExplore()) {
                    searchData(typeName = viewModel.getTypes()[0])
                } else {
                    viewModel.toastMsg("动态书源仅支持搜索")
                }
                return@setOnClickListener
            }
            MaterialDialog(requireContext()).show {
                title(text = "小说分类")
                listItems(items = viewModel.getTypes()) { _, _, text ->
                    searchData(typeName = text.toString())
                }
                lifecycleOwner(requireActivity())
            }
        }

        // 设置搜索事件
        binding.searchTitle.tvSearchSearch.setOnClickListener {
            MaterialDialog(requireContext()).show {
                title(text = "搜索小说")
                input(
                    hint = "输入 书名、作者",
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
                ) { _, text ->
                    val keyword = text.toString()
                    if (keyword.isNotBlank()) {
                        searchData(keyWord = keyword)
                    } else {
                        viewModel.toastMsg("关键字不能为空")
                    }
                }
                positiveButton(text = "搜索")
            }
        }

        // 刷新事件
        binding.refreshLayout.setOnRefreshListener {
            viewModel.fetchSearch(1)
        }

        // 点击项目事件
        squareAdapter.itemClickListener = object : SquareAdapter.OnBookItemClickListener {
            override fun openItem(t: BookBean) {
                viewModel.fetchBookInfo(t.url)
            }
        }

        //添加列表滚动事件
        binding.rvTypes.addOnScrollListener(object :
            RVOScrollListener(binding.rvTypes.layoutManager as LinearLayoutManager) {
            override fun loadMoreItems() {
                viewModel.fetchSearch()
            }

            override fun totalPageCount(): Int {
                return viewModel.getTotalPage()
            }

            override fun isLastPage(): Boolean {
                return viewModel.isLastPage()
            }

            override fun isLoading(): Boolean {
                return viewModel.isLoading()
            }
        })

        //错误通知事件
        viewModel.toast.observe(viewLifecycleOwner, {
            if (!it.isNullOrBlank()) {
                Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
            }
        })

        //书城空态或错误提示
        viewModel.squareMessage.observe(viewLifecycleOwner, {
            showSquareMessage(it)
        })

        viewModel.sourceOptions.observe(viewLifecycleOwner, {
            viewModel.updateSourceOptions(it)
            val selectedChanged = viewModel.selectDefaultSourceIfNeeded(it)
            if (it.isNotEmpty() && (selectedChanged || squareAdapter.itemCount == 0) && !viewModel.isDynamicSource()) {
                val typeName = viewModel.getTypes()[0]
                searchData(typeName = typeName)
            }
        })

        //是否显示加载框
        viewModel.isLoading.observe(viewLifecycleOwner, {
            binding.refreshLayout.isRefreshing = it
        })

        //根据类型搜索
        viewModel.searchType.observe(viewLifecycleOwner, {
            fetchPage(it.currentPage, it.totalPage,it.bookBeans)
        })

        //根据关键字搜索
        viewModel.searchKeyWord.observe(viewLifecycleOwner, {
            fetchPage(it.currentPage, it.totalPage,it.bookBeans)
        })

        //新增书籍
        viewModel.bookInserted.observe(viewLifecycleOwner, {
            viewModel.toastMsg("加入书架成功")
        })

        //更新书籍信息
        viewModel.bookInfo.observe(viewLifecycleOwner, {
            if (it?.bookBean != null) {
                val bookBean = it.bookBean
                viewModel.fetchBook(bookBean)
                //小说详情
                MaterialDialog(requireContext()).show {
                    title(text = bookBean.name)
                    //icon(R.mipmap.ic_launcher)
                    //icon(drawable = image)
                    message(text = "作者：${bookBean.author}\n类别：${bookBean.category}\n状态：${bookBean.status}\n简介：${bookBean.desc}")
                    positiveButton(text = "开始阅读") {
                        val selectedBook = viewModel.getBookBean()
                        if (selectedBook != null) {
                            // 打开 书籍
                            NovelReadActivity.startFromActivity(requireActivity(), selectedBook)
                        } else {
                            viewModel.toastMsg("打开书籍异常，请重新获取书籍")
                        }
                    }
                    negativeButton(text = "加入书架") {
                        //加入书架
                        viewModel.insertBook()
                    }
                    lifecycleOwner(requireActivity())
                }
            }
        })
    }

    /**
     * 初始化数据
     */
    override fun initData() {}

    override fun onResume() {
        super.onResume()
        viewModel.fetchSourceOptions()
    }

    /**
     * 填充页面
     */
    private fun fetchPage(currentPage:Int,totalPage:Int,bookList:List<BookBean>) {
        showSquareMessage(null)
        if (currentPage == 1) {
            squareAdapter.refreshItems(bookList)
        } else {
            if (bookList.isNotEmpty()) {
                squareAdapter.addItems(bookList)
            }
        }
        viewModel.fetchPage(currentPage + 1, totalPage)
        binding.refreshLayout.isRefreshing = false
    }

    /**
     * 加载数据
     */
    private fun searchData(
        keyWord: String = "",
        typeName: String = ""
    ) {
        if (keyWord.isBlank()) {
            // 根据类型搜索
            viewModel.fetchSearchType(typeName, 1)
            binding.searchTitle.tvSearchTitle.text = viewModel.getShopTitle()
            binding.searchTitle.tvSearchFilter.text = typeName
        } else {
            //根据关键字搜索
            viewModel.fetchSearchKeyWord(keyWord, 1)
            binding.searchTitle.tvSearchTitle.text = viewModel.getShopTitle()
            binding.searchTitle.tvSearchFilter.text = keyWord
        }
    }

    private fun showSquareMessage(message: String?) {
        if (message.isNullOrBlank()) {
            binding.tvSquareMessage.text = ""
            binding.tvSquareMessage.visibility = View.GONE
            binding.rvTypes.visibility = View.VISIBLE
        } else {
            binding.tvSquareMessage.text = message
            binding.tvSquareMessage.visibility = View.VISIBLE
            binding.rvTypes.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

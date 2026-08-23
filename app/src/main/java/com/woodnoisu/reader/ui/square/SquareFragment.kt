package com.woodnoisu.reader.ui.square

import android.annotation.SuppressLint
import android.content.Intent
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.viewModels
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
import com.woodnoisu.reader.ui.source.BookSourceActivity
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
            val options = viewModel.getSourceOptionsSnapshot()
            if (options.isEmpty()) {
                openBookSourceManagement()
                return@setOnClickListener
            }
            MaterialDialog(requireContext()).show {
                title(text = "书城分类")
                listItems(items = options.map { it.name }) { _, index, _ ->
                    val option = options[index]
                    viewModel.selectShopOption(option)
                    binding.searchTitle.tvSearchTitle.text = viewModel.getShopTitle()
                    if (option.canExplore) {
                        searchData(typeName = viewModel.getTypes()[0])
                    } else {
                        squareAdapter.clear()
                        binding.searchTitle.tvSearchFilter.text = "仅搜索"
                        viewModel.showSearchOnlyMessage()
                    }
                }
                lifecycleOwner(requireActivity())
            }
        }

        // 小说分类弹出框事件
        binding.searchTitle.tvSearchFilter.setOnClickListener {
            if (!viewModel.hasShopName()) {
                openBookSourceManagement()
                return@setOnClickListener
            }
            if (viewModel.canExplore()) {
                searchData(typeName = viewModel.getTypes()[0])
            } else {
                viewModel.toastMsg("当前书源仅支持搜索")
                return@setOnClickListener
            }
        }

        // 设置搜索事件
        binding.searchTitle.tvSearchSearch.setOnClickListener {
            if (!viewModel.hasShopName()) {
                viewModel.showNoSourceMessage()
                return@setOnClickListener
            }
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
            if (viewModel.hasShopName()) {
                viewModel.fetchSearch(1)
            } else {
                viewModel.fetchSourceOptions()
            }
        }

        binding.tvSquareMessage.setOnClickListener {
            if (!viewModel.hasShopName() && viewModel.getSourceOptionsSnapshot().isEmpty()) {
                openBookSourceManagement()
            }
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
            if (it.isEmpty()) {
                squareAdapter.clear()
                binding.searchTitle.tvSearchTitle.text = viewModel.getShopTitle()
                binding.searchTitle.tvSearchFilter.text = "书源管理"
                showSquareMessage(SquareViewModel.NO_SOURCE_MESSAGE)
            } else if (viewModel.canExplore()) {
                val typeName = viewModel.getTypes()[0]
                binding.searchTitle.tvSearchTitle.text = viewModel.getShopTitle()
                binding.searchTitle.tvSearchFilter.text = typeName
                if (selectedChanged || squareAdapter.itemCount == 0) {
                    searchData(typeName = typeName)
                }
            } else {
                binding.searchTitle.tvSearchTitle.text = viewModel.getShopTitle()
                binding.searchTitle.tvSearchFilter.text = "仅搜索"
                if (selectedChanged) {
                    squareAdapter.clear()
                }
                if (squareAdapter.itemCount == 0) {
                    viewModel.showSearchOnlyMessage()
                }
            }
            binding.refreshLayout.isRefreshing = false
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
        if (!viewModel.hasShopName()) {
            viewModel.showNoSourceMessage()
            binding.searchTitle.tvSearchTitle.text = viewModel.getShopTitle()
            binding.searchTitle.tvSearchFilter.text = "书源管理"
            return
        }
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
            binding.tvSquareMessage.isClickable = false
            binding.rvTypes.visibility = View.VISIBLE
        } else {
            binding.tvSquareMessage.text = message
            binding.tvSquareMessage.visibility = View.VISIBLE
            binding.tvSquareMessage.isClickable = !viewModel.hasShopName() &&
                viewModel.getSourceOptionsSnapshot().isEmpty()
            binding.rvTypes.visibility = View.GONE
        }
    }

    private fun openBookSourceManagement() {
        startActivity(Intent(requireContext(), BookSourceActivity::class.java))
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

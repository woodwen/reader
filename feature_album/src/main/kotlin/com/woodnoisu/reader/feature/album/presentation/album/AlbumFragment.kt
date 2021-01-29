package com.woodnoisu.reader.feature.album.presentation.album

import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.pawegio.kandroid.visible
import com.woodnoisu.reader.feature.album.R
import com.woodnoisu.reader.feature.album.domain.model.AlbumBookDomainModel
import com.woodnoisu.reader.feature.album.presentation.album.recyclerview.AlbumAdapter
import com.woodnoisu.reader.feature.album.presentation.album.recyclerview.AlbumScrollListener
import com.woodnoisu.reader.library.base.presentation.extension.observe
import com.woodnoisu.reader.library.base.presentation.fragment.InjectionFragment
import com.woodnoisu.reader.library.base.utils.DialogUtil
import com.woodnoisu.reader.library.base.utils.showToast
import kotlinx.android.synthetic.main.fragment_album_square.*
import kotlinx.android.synthetic.main.search_title.*
import org.kodein.di.generic.instance

/**
 * 广场页
 */
class AlbumFragment : InjectionFragment(R.layout.fragment_album_square) {

    private val viewModel: AlbumViewModel by instance()

    private val albumAdapter: AlbumAdapter by instance()

    private val stateObserver = Observer<ViewState> {
        showToast(it.errorMsg)
        progressBar.visible = it.isLoading
        val currentPage = it.albumDomainModel.currentPage
        val totalPage = it.albumDomainModel.totalPage
        val book = it.albumDomainModel.albumBookDomainModel
        val bookList = it.albumDomainModel.albumBookDomainModels

        // 加载书籍列表
        if (bookList != null) {
            if (bookList.isNotEmpty()) {
                if (currentPage == 1) {
                    albumAdapter.refreshItems(bookList)
                } else {
                    albumAdapter.addItems(bookList)
                }
            }
            viewModel.setPage(currentPage + 1, totalPage)
        }

        //加载书籍
        if (book != null) {
            DialogUtil.showDialog(
                    activity = requireActivity(),
                    title = book.name,
                    message = "作者：${book.author}\n类别：${book.category}\n状态：${book.status}\n简介：${book.desc}",
                    positiveButtonText = "开始阅读",
                    negativeButtonText = "加入书架",
                    onPositiveClick = {
                        // 打开 书籍
                        viewModel.insertBook(book, true)
                    },
                    onNegativeClick = {
                        //加入书架
                        viewModel.insertBook(book)
                    }
            )
        }
    }

    /**
     * 初始化界面
     */
    override fun initView() {
        // 初始化主显示界面
        rv_types.apply {
            adapter = albumAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(object :
                    AlbumScrollListener(rv_types.layoutManager as LinearLayoutManager) {
                override fun loadMoreItems(){
                    progressBar.visible = true
                    viewModel.getBookList()
                }
                override fun isLastPage() = viewModel.isLastPage()
                override fun isLoading() = progressBar.visible
            })
        }
    }

    /**
     * 初始化监听
     */
    override fun initListener() {
        // 小说书城弹出框事件
        tv_search_title.setOnClickListener {
            DialogUtil.showDialog(
                    activity = requireActivity(),
                    title = "书城分类",
                    list = viewModel.getParses(),
                    onListItemClick = { _,text->
                        progressBar.visible = true
                        viewModel.setShopName(text)
                        val typeName = viewModel.getTypes()[0]
                        viewModel.getBookListByType(typeName, 1)
                        refreshTitle()
                    }
            )
        }

        // 小说分类弹出框事件
        tv_search_filter.setOnClickListener {
            DialogUtil.showDialog(
                    activity = requireActivity(),
                    title = "小说分类",
                    list = viewModel.getTypes(),
                    onListItemClick = {_,text->
                        progressBar.visible = true
                        viewModel.getBookListByType(text, 1)
                        refreshTitle()
                    }
            )
        }

        // 设置搜索事件
        tv_search_search.setOnClickListener {
            DialogUtil.showDialog(
                    activity = requireActivity(),
                    title = "搜索小说",
                    hint = "输入 书名、作者",
                    positiveButtonText = "搜索",
                    onInput = {
                        if (it.isNotBlank()) {
                            progressBar.visible = true
                            viewModel.getBookListByKeyWord(it, 1)
                            refreshTitle()
                        }
                    }
            )
        }

        // 点击项目事件
        albumAdapter.itemClickListener = object : AlbumAdapter.OnBookItemClickListener {
            override fun openItem(albumBook: AlbumBookDomainModel) {
                viewModel.getBook(albumBook.url)
            }
        }

        // 监听变化
        observe(viewModel.stateLiveData, stateObserver)
    }

    /**
     * 初始化数据
     */
    override fun initData() {
        // 填充默认数据
        progressBar.visible = true
        viewModel.loadData()
        refreshTitle()
    }

    /**
     * 刷新
     */
    private fun refreshTitle() {
        // 设置标签
        tv_search_title.text = viewModel.getShopName()
        tv_search_filter.text = viewModel.getKeywordOrTypeName()
    }
}
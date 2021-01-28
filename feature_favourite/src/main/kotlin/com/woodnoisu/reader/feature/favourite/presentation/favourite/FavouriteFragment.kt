package com.woodnoisu.reader.feature.favourite.presentation.favourite

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.woodnoisu.reader.feature.favourite.R
import com.woodnoisu.reader.feature.favourite.domain.model.FavouriteBookDomainModel
import com.woodnoisu.reader.feature.favourite.presentation.favourite.recyclerview.ShelfAdapter
import com.woodnoisu.reader.library.base.presentation.extension.observe
import com.woodnoisu.reader.library.base.presentation.fragment.InjectionFragment
import com.woodnoisu.reader.library.base.utils.HtFileUtils
import com.woodnoisu.reader.library.base.utils.showToast
import kotlinx.android.synthetic.main.fragment_favourites.*
import org.kodein.di.generic.instance

class FavouriteFragment : InjectionFragment(R.layout.fragment_favourites) {

    private val viewModel: FavouriteViewModel by instance()

    private val favouriteAdapter: ShelfAdapter by instance()

    private val stateObserver = Observer<ViewState> {
        refresh_layout.isRefreshing = it.isLoading
        showToast(it.errorMsg)
        val bookDomainModels = it.favouriteDomainModel.favouriteBookDomainModels
        favouriteAdapter.refreshItems(bookDomainModels)
    }

    /**
     * 初始化界面
     */
    override fun initView(){
        refresh_layout.apply {
            // 初始化刷新颜色(滚动框)
            setColorSchemeResources(R.color.colorAccent)
        }

        rv_shelf.apply {
            // 加载框
            layoutManager = GridLayoutManager(context, 3)
            adapter = favouriteAdapter
        }
    }

    /**
     * 初始化监听
     */
    override fun initListener(){
        // 获取本地书籍
        val startActivityLaunch = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            val uri = it
            if (context != null && uri != null) {
                val name = HtFileUtils.uriToName(uri, requireContext())
                //需要加入修改
                val path = HtFileUtils.getFilePathForN(uri,  requireContext())
                if(!path.isNullOrBlank()){
                    val bookModel = FavouriteBookDomainModel()
                    bookModel.name = name
                    bookModel.bookFilePath = path
                    // 新增书籍
                    viewModel.insertBook(bookModel)
                    favouriteAdapter.addItem(bookModel)
                }
            }
        }

        // 书籍适配器 项目点击事件
        favouriteAdapter.itemPositionClickListener = object : ShelfAdapter.OnItemPositionClickListener {
            /**
             * 打开项目
             */
            override fun openItem(position: Int, favouriteBookDomainModel: FavouriteBookDomainModel) {
                viewModel.navigateToReader(favouriteBookDomainModel.id.toString())
            }

            /**
             * 删除项目
             */
            override fun deleteItem(position: Int, favouriteBookDomainModel: FavouriteBookDomainModel) {
                // 删除书籍
                viewModel.deleteBook(favouriteBookDomainModel)
                favouriteAdapter.removeItem(favouriteBookDomainModel)
            }
        }

        // 设置刷新事件
        refresh_layout.setOnRefreshListener {
            // 刷新数据
            refresh_layout.isRefreshing = true
            viewModel.loadData()
        }

        // 设置搜索框内容变更事件
        et_search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.getBookList(s.toString().trim())
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
                        favouriteAdapter.edit = true
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
            favouriteAdapter.edit = false
        }

        // 监听事件
        observe(viewModel.stateLiveData, stateObserver)
    }

    /**
     * 初始化数据
     */
    override fun initData(){
        //填充默认数据
        viewModel.loadData()
    }
}

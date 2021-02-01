package com.woodnoisu.reader.ui.widget.page.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.woodnoisu.reader.ui.widget.page.model.PageStyle
import com.woodnoisu.reader.R
import com.woodnoisu.reader.ui.widget.page.PageLoader
import com.woodnoisu.reader.ui.widget.page.adapter.PageStyleHolder

/**
 * 页面风格适配器
 */
class PageStyleAdapter(private val mList: List<Drawable>, private val mPageLoader: PageLoader) :
    RecyclerView.Adapter<PageStyleHolder>() {
    // 上下文
    private var mContext: Context? = null
    // 当前选中
    private var currentChecked: Int = 0

    /**
     * 创建holder
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): PageStyleHolder {
        if (mContext == null) {
            mContext = viewGroup.context
        }
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_read_bg, viewGroup, false)
        return PageStyleHolder(view)
    }

    /**
     * 绑定holder
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onBindViewHolder(pageHolder: PageStyleHolder, i: Int) {
        pageHolder.mReadBg.background = mList[i]
        pageHolder.mIvChecked.visibility = View.GONE
        if (currentChecked == i) {
            pageHolder.mIvChecked.visibility = View.VISIBLE
        }
        pageHolder.itemView.setOnClickListener {
            currentChecked = i
            notifyDataSetChanged()
            mPageLoader.setPageStyle(PageStyle.values()[i])
        }
    }

    /**
     * 获取项目数量
     */
    override fun getItemCount(): Int {
        return mList.size
    }

    /**
     * 设置当前选中
     */
    fun setPageStyleChecked(pageStyle: PageStyle) {
        currentChecked = pageStyle.ordinal
    }
}

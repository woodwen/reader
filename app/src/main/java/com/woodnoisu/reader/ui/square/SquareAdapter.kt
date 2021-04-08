package com.woodnoisu.reader.ui.square

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.woodnoisu.reader.R
import com.woodnoisu.reader.model.BookBean

abstract class RVOScrollListener constructor(var layoutManager: LinearLayoutManager) : RecyclerView.OnScrollListener() {

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        val visibleItemCount: Int = layoutManager.childCount
        val totalItemCount: Int = layoutManager.itemCount
        val firstVisibleItemPosition: Int = layoutManager.findFirstVisibleItemPosition()

        if (newState == RecyclerView.SCROLL_STATE_IDLE && !isLoading() && !isLastPage()
            && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount
            && firstVisibleItemPosition >= 0) {
            loadMoreItems()
        }
    }

    abstract fun loadMoreItems()

    abstract fun totalPageCount(): Int

    abstract fun isLastPage(): Boolean
    abstract fun isLoading(): Boolean
}

/**
 * 书籍列表适配器
 */
class SquareAdapter: RecyclerView.Adapter<SquareAdapter.ViewHolder>() {
    // 上下文
    private var mContext: Context? = null

    //列表容器
    private val mList: MutableList<BookBean> =ArrayList()

    /**
     * 内部类
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var ivCover: ImageView = itemView.findViewById(R.id.iv_item_cover)
        var tvName: TextView = itemView.findViewById(R.id.tv_item_name)
        var tvAuthor: TextView = itemView.findViewById(R.id.tv_item_author)
        var tvDesc: TextView = itemView.findViewById(R.id.tv_item_desc)

    }

    interface OnBookItemClickListener {
        /**
         * 打开事件
         */
        fun openItem(t: BookBean)
    }

    /**
     * 点击项目事件
     */
    var itemClickListener: OnBookItemClickListener? = null

    /**
     * 创建事件
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (mContext == null) {
            mContext = parent.context
        }
        return ViewHolder(
            LayoutInflater.from(mContext).inflate(R.layout.item_book, parent, false)
        )
    }

    /**
     * 绑定
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookModel = mList[position]
        holder.tvName.text = bookModel.name
        holder.tvAuthor.text = "作者：" + bookModel.author
        holder.tvDesc.text = bookModel.desc
        holder.ivCover.load(bookModel.cover)
        holder.itemView.setOnClickListener {
            itemClickListener?.openItem(bookModel)
        }
    }

    /**
     * 获取数量
     */
    override fun getItemCount(): Int = mList.size

    /**
     * 添加项目
     */
    fun addItem(value: BookBean) {
        mList.add(value)
        notifyDataSetChanged()
    }

    /**
     * 添加指定位置的项目
     */
    fun addItem(index: Int, value: BookBean) {
        mList.add(index, value)
        notifyDataSetChanged()
    }

    /**
     * 批量添加项目
     */
    fun addItems(values: List<BookBean>) {
        mList.addAll(values)
        notifyDataSetChanged()
    }

    /**
     * 移除项目
     */
    fun removeItem(value: BookBean) {
        mList.remove(value)
        notifyDataSetChanged()
    }

    /**
     * 刷新容器
     */
    fun refreshItems(list: List<BookBean>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    /**
     * 清理容器
     */
    fun clear() {
        mList.clear()
    }
}
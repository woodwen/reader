package com.woodnoisu.reader.ui.shelf

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.woodnoisu.reader.R
import com.woodnoisu.reader.model.BookBean

/**
 * 书架适配器
 */
class ShelfAdapter : RecyclerView.Adapter<ShelfAdapter.ShelfViewHolder>() {

    // 上下文
    private var mContext: Context? = null

    //列表容器
    private val mList: MutableList<BookBean> =ArrayList()

    // 编辑内容
    var edit: Boolean = false
        set(edit) {
            field = edit
            notifyDataSetChanged()
        }

    //项目点击事件
    var itemPositionClickListener: OnItemPositionClickListener? = null


    interface OnItemPositionClickListener {
        /**
         * 打开事件
         */
        fun openItem(position: Int, t: BookBean)

        /**
         * 删除事件
         */
        fun deleteItem(position: Int, t: BookBean)
    }

    class ShelfViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCover: ImageView = itemView.findViewById(R.id.iv_item_cover)
        val tvName: TextView = itemView.findViewById(R.id.tv_item_name)
        val ivDelete: ImageView = itemView.findViewById(R.id.iv_item_del)
    }

    /**
     * 创建holder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShelfViewHolder {
        if (mContext == null) {
            mContext = parent.context
        }
        return ShelfViewHolder(
            LayoutInflater.from(mContext).inflate(R.layout.item_shelf, parent, false)
        )
    }

    /**
     * 绑定Holder
     */
    override fun onBindViewHolder(holder: ShelfViewHolder, position: Int) {
        if(holder is ShelfViewHolder){
            holder.ivDelete.isVisible = edit
            val bookModel = mList[position]
            // 如果是编辑模式则变暗
            holder.ivCover.alpha = if (bookModel.url.isNotEmpty() && edit) 0.5f else 1f
            // 有图片则显示图片，没有则显示默认
            if (bookModel.cover.isNotBlank()) {
//                ImageLoader.loadImage(mContext!!, holder.ivCover, bookModel.cover)
                holder.ivCover.load( bookModel.cover)
            } else {
                holder.ivCover.setImageResource(R.drawable.ic_no_thumb)
            }
            // 设置书籍名称
            holder.tvName.text = bookModel.name
            if (edit) {
                // 如果编辑模式下，则可以删除书籍
                holder.itemView.setOnClickListener {
                    itemPositionClickListener?.deleteItem(position,bookModel)
                }
            } else {
                // 如果非编辑模式下，则打开书籍
                holder.itemView.setOnClickListener {
                    itemPositionClickListener?.openItem(position,bookModel)
                }
            }
        }
    }

    /**
     * 获取项目数量
     */
    override fun getItemCount(): Int {
        return mList.size
    }

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
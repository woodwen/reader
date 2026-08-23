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

class RemoteSearchAdapter : RecyclerView.Adapter<RemoteSearchAdapter.ViewHolder>() {
    private var mContext: Context? = null
    private val mList: MutableList<BookBean> = ArrayList()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivCover: ImageView = itemView.findViewById(R.id.iv_item_cover)
        val tvName: TextView = itemView.findViewById(R.id.tv_item_name)
        val tvAuthor: TextView = itemView.findViewById(R.id.tv_item_author)
        val tvSource: TextView = itemView.findViewById(R.id.tv_item_source)
        val tvCategory: TextView = itemView.findViewById(R.id.tv_item_category)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_item_status)
        val tvWordCount: TextView = itemView.findViewById(R.id.tv_item_word_count)
        val tvLatestChapter: TextView = itemView.findViewById(R.id.tv_item_latest_chapter)
        val tvDesc: TextView = itemView.findViewById(R.id.tv_item_desc)
    }

    interface OnBookItemClickListener {
        fun openItem(t: BookBean)
    }

    var itemClickListener: OnBookItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (mContext == null) {
            mContext = parent.context
        }
        return ViewHolder(
            LayoutInflater.from(mContext).inflate(R.layout.item_remote_search_book, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookModel = mList[position]
        holder.tvName.text = bookModel.name
        holder.tvAuthor.text = "作者：" + bookModel.author.ifBlank { "未知" }
        holder.tvSource.text = "来源：" + bookModel.sourceDisplayName.ifBlank {
            bookModel.shopName.ifBlank { "未知" }
        }
        holder.tvCategory.text = "分类：" + bookModel.category.ifBlank { "未知" }
        holder.tvStatus.text = "状态：" + bookModel.status.ifBlank { "未知" }
        bindOptional(holder.tvWordCount, "字数：", bookModel.wordCountText)
        bindOptional(holder.tvLatestChapter, "最新：", bookModel.latestChapter)
        holder.tvDesc.text = bookModel.desc
        holder.tvDesc.isVisible = bookModel.desc.isNotBlank()
        if (bookModel.cover.isBlank()) {
            holder.ivCover.setImageResource(R.drawable.pic_placeholder)
        } else {
            holder.ivCover.load(bookModel.cover) {
                placeholder(R.drawable.pic_placeholder)
                error(R.drawable.pic_placeholder)
            }
        }
        holder.itemView.setOnClickListener {
            itemClickListener?.openItem(bookModel)
        }
    }

    override fun getItemCount(): Int = mList.size

    fun addItems(values: List<BookBean>) {
        mList.addAll(values)
        notifyDataSetChanged()
    }

    fun refreshItems(list: List<BookBean>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    fun clear() {
        mList.clear()
        notifyDataSetChanged()
    }

    private fun bindOptional(textView: TextView, label: String, value: String) {
        textView.isVisible = value.isNotBlank()
        textView.text = label + value
    }
}

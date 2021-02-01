package com.woodnoisu.reader.ui.novelRead

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.woodnoisu.reader.R
import com.woodnoisu.reader.model.BookSignBean
import java.util.ArrayList

/**
 * 笔记适配
 */
class MarkAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class MarkHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 标签对象
        var mTvMark: TextView = itemView.findViewById(R.id.tvMarkItem)
        // 标签选择框
        var mCheck: CheckBox = itemView.findViewById(R.id.checkbox)
    }

    // 上下文
    private var mContext: Context? = null

    // 标记队列
    private val mList : MutableList<BookSignBean>  = ArrayList()

    // 编辑内容
    var edit: Boolean = false
        set(edit) {
            field = edit
            notifyDataSetChanged()
        }

    // 选择列表
    val selectList: List<BookSignBean>
        get() {
            return mList.filter {
                return@filter it.edit
            }
        }

    /**
     * 创建holder
     */
    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RecyclerView.ViewHolder {
        if (mContext == null) {
            mContext = viewGroup.context
        }
        val view: View =
            LayoutInflater.from(mContext).inflate(R.layout.rlv_item_mark, viewGroup, false)
        return MarkHolder(view)
    }

    /**
     * 绑定Holder
     */
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, i: Int) {
        if (viewHolder is MarkHolder) {
            if (this.edit) {
                viewHolder.mCheck.visibility = View.VISIBLE
                viewHolder.mCheck.setOnCheckedChangeListener { compoundButton, b ->
                    mList[i].edit = b
                }
            } else {
                viewHolder.mCheck.visibility = View.GONE
            }
            viewHolder.mTvMark.text = mList[i].chapterName
            viewHolder.mCheck.isChecked = mList[i].edit
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
    fun addItem(value: BookSignBean) {
        mList.add(value)
        notifyDataSetChanged()
    }

    /**
     * 添加指定位置的项目
     */
    fun addItem(index: Int, value: BookSignBean) {
        mList.add(index, value)
        notifyDataSetChanged()
    }

    /**
     * 批量添加项目
     */
    fun addItems(values: List<BookSignBean>) {
        mList.addAll(values)
        notifyDataSetChanged()
    }

    /**
     * 移除项目
     */
    fun removeItem(value: BookSignBean) {
        mList.remove(value)
        notifyDataSetChanged()
    }

    /**
     * 刷新容器
     */
    fun refreshItems(list: List<BookSignBean>) {
        mList.clear()
        mList.addAll(list)
        notifyDataSetChanged()
    }

    /**
     * 清理容器
     */
    fun clear() {
        mList.clear()
        notifyDataSetChanged()
    }
}
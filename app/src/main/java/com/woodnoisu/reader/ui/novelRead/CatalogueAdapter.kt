package com.woodnoisu.reader.ui.novelRead

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.woodnoisu.reader.R
import com.woodnoisu.reader.model.ChapterBean
import com.woodnoisu.reader.utils.BookUtil
import com.woodnoisu.reader.utils.MD5Util
import com.woodnoisu.reader.utils.StringUtil
import java.util.*

/**
 * 目录适配
 */
class CatalogueAdapter : BaseAdapter() {

    /**
     * 目录Holder
     */
    class CatalogueHolder {
        // 页面
        private lateinit var itemView: View

        // 上下文
        private lateinit var context: Context

        // 章节标题
        private var mTvChapter: TextView? = null

        /**
         * 创建项目
         */
        fun createItemView(parent: ViewGroup): View {
            itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.rlv_item_catalogue, parent, false)
            context = parent.context
            mTvChapter = itemView.findViewById(R.id.category_tv_chapter)
            return itemView
        }

        /**
         * 绑定
         */
        fun onBind(value: ChapterBean, pos: Int) {
            //首先判断是否该章已下载
            var drawable: Drawable? = null
            drawable = if (!TextUtils.isEmpty(value.bookUrl) && BookUtil.isChapterCached(
                    MD5Util.strToMd5By16(
                        value.bookUrl
                    ), value.name
                )
            ) {
                ContextCompat.getDrawable(
                    context,
                    R.drawable.selector_category_load
                )
            } else {
                ContextCompat.getDrawable(
                    context,
                    R.drawable.selector_category_unload
                )
            }
            mTvChapter?.isSelected = false
            mTvChapter?.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.colorTitle
                )
            )
            mTvChapter?.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
            mTvChapter?.text = StringUtil.convertCC(value.name)
        }

        /**
         * 点击事件
         */
        fun onClick() {}

        /**
         * 设置选择章节
         */
        fun setSelectedChapter() {
            mTvChapter?.setTextColor(
                ContextCompat.getColor(context, R.color.light_red)
            )
            mTvChapter?.isSelected = true
        }
    }

    // 当前选择
    private var currentSelected = 0

    //列表容器
    private val mList: MutableList<ChapterBean> = ArrayList()

    private var listView: ListView? = null

    /**
     * 获取界面
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        if (listView == null && parent is ListView) {
            listView = parent
        }
        var convertView: View? = convertView
        val holder: CatalogueHolder
        if (convertView == null) {
            holder = CatalogueHolder()
            convertView = holder.createItemView(parent)
            convertView.tag = holder
        } else {
            holder = convertView.tag as CatalogueHolder
        }
        //执行绑定
        holder.onBind(getItem(position), position)

        //设置选中的章节
        if (position == currentSelected) {
            holder.setSelectedChapter()
        }
        return convertView
    }

    /**
     * 获取项目
     */
    override fun getItem(position: Int): ChapterBean {
        return mList[position]
    }

    /**
     * 获取项目id
     */
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    /**
     * 获取数量
     */
    override fun getCount(): Int {
        return mList.size
    }

    /**
     * 添加项目
     */
    fun addItem(value: ChapterBean) {
        mList.add(value)
        notifyDataSetChanged()
    }

    /**
     * 添加指定位置的项目
     */
    fun addItem(index: Int, value: ChapterBean) {
        mList.add(index, value)
        notifyDataSetChanged()
    }

    /**
     * 批量添加项目
     */
    fun addItems(values: List<ChapterBean>) {
        mList.addAll(values)
        notifyDataSetChanged()
    }

    /**
     * 移除项目
     */
    fun removeItem(value: ChapterBean) {
        mList.remove(value)
        notifyDataSetChanged()
    }

    /**
     * 刷新项目
     */
    fun refreshItem(id: Int) {
        var pos = -1
        for (index in mList.indices) {
            val item = mList[index]
            if (id == item.id) {
                pos = index
                break
            }
        }
        if (pos >= 0) {
            /**第一个可见的位置**/
            val firstVisiblePosition = listView?.firstVisiblePosition ?: 0

            /**最后一个可见的位置**/
            val lastVisiblePosition = listView?.lastVisiblePosition ?: 0

            /**在看见范围内才更新，不可见的滑动后自动会调用getView方法更新**/
            if (pos in firstVisiblePosition..lastVisiblePosition) {
                /**获取指定位置view对象**/
                val view = listView?.getChildAt(pos - firstVisiblePosition)
                getView(pos, view, listView as ViewGroup)
            }
        }
    }

    /**
     * 刷新容器
     */
    fun refreshItems(list: List<ChapterBean>) {
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

    /**
     * 设置章节
     */
    fun setChapter(pos: Int) {
        currentSelected = pos
        notifyDataSetChanged()
    }

    /**
     * 获取章节
     */
    fun getChapter(pos: Int): ChapterBean {
        return mList[pos]
    }
}
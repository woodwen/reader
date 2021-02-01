package com.woodnoisu.reader.ui.widget.page.adapter

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.woodnoisu.reader.R

/**
 * 页面风格Holder
 */
class PageStyleHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    // 阅读背景
    val mReadBg: View = itemView.findViewById(R.id.read_bg_view)
    // 背景选中图
    val mIvChecked: ImageView = itemView.findViewById(R.id.read_bg_iv_checked)
}
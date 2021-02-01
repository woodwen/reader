package com.woodnoisu.reader.ui.main

import android.util.SparseArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.woodnoisu.reader.ui.me.MeFragment
import com.woodnoisu.reader.ui.shelf.ShelfFragment
import com.woodnoisu.reader.ui.square.SquareFragment


class MainAdapter(fragmentActivity: FragmentActivity)
    : FragmentStateAdapter(fragmentActivity) {
    // 基础窗口
    private val fragments: SparseArray<Fragment> = SparseArray(3)

    // 初始化
    init {
        fragments.append(0, SquareFragment())
        fragments.append(1, ShelfFragment())
        fragments.append(2, MeFragment())
    }

    // 重新获取项目序号
    override fun getItemCount(): Int {
        return fragments.size()
    }

    //创建fragment
    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}
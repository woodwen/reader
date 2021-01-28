package com.woodnoisu.reader.feature.reader.presentation.page

import android.content.Context
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.woodnoisu.reader.feature.reader.R

class AnimManager(context: Context) {
    // 工具栏进入动画
    var mTopInAnim: Animation

    // 工具栏退出动画
    var mTopOutAnim: Animation

    // 底部退出动画
    var mBottomInAnim: Animation

    // 底部进入动画
    var mBottomOutAnim: Animation

    init {
        // 初始化动画
        mTopInAnim = AnimationUtils.loadAnimation(context, R.anim.slide_top_in)
        mTopOutAnim = AnimationUtils.loadAnimation(context, R.anim.slide_top_out)
        mBottomInAnim = AnimationUtils.loadAnimation(context, R.anim.slide_bottom_in)
        mBottomOutAnim = AnimationUtils.loadAnimation(context, R.anim.slide_bottom_out)

        //退出的速度要快
        mTopOutAnim.duration = 200
        mBottomOutAnim.duration = 200
    }
}
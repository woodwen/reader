package com.woodnoisu.reader.ui.widget.page

import com.woodnoisu.reader.ui.widget.page.model.PageMode
import com.woodnoisu.reader.ui.widget.page.model.PageStyle
import com.woodnoisu.reader.utils.ScreenUtil
import com.woodnoisu.reader.utils.SpUtil

/**
 * 阅读配置管理
 */
class ReadSettingManager private constructor() {

    /**
     * 静态相关
     */
    companion object {
        /*************实在想不出什么好记的命名方式。。 */
        const val READ_BG_DEFAULT = 0
        const val READ_BG_1 = 1
        const val READ_BG_2 = 2
        const val READ_BG_3 = 3
        const val READ_BG_4 = 4
        const val NIGHT_MODE = 5

        const val SHARED_READ_BG = "shared_read_bg"
        const val SHARED_READ_BRIGHTNESS = "shared_read_brightness"
        const val SHARED_READ_IS_BRIGHTNESS_AUTO = "shared_read_is_brightness_auto"
        const val SHARED_READ_TEXT_SIZE = "shared_read_text_size"
        const val SHARED_READ_IS_TEXT_DEFAULT = "shared_read_text_default"
        const val SHARED_READ_PAGE_MODE = "shared_read_mode"
        const val SHARED_READ_NIGHT_MODE = "shared_night_mode"
        const val SHARED_READ_VOLUME_TURN_PAGE = "shared_read_volume_turn_page"
        const val SHARED_READ_FULL_SCREEN = "shared_read_full_screen"
        const val SHARED_READ_CONVERT_TYPE = "shared_read_convert_type"

        @Volatile
        private var instance: ReadSettingManager? = null

        @Synchronized
        fun getInstance(): ReadSettingManager {
            if (instance == null) {
                instance = ReadSettingManager()
            }
            return instance as ReadSettingManager
        }
    }

    /**
     * 设置亮度
     */
    var brightness: Int
        get() = SpUtil.getIntValue(SHARED_READ_BRIGHTNESS, 40)
        set(progress) = SpUtil.setIntValue(SHARED_READ_BRIGHTNESS, progress)

    /**
     * 是否自动调节亮度
     */
    var isBrightnessAuto: Boolean
        get() = SpUtil.getBooleanValue(SHARED_READ_IS_BRIGHTNESS_AUTO, false)
        set(isAuto) = SpUtil.setBooleanValue(SHARED_READ_IS_BRIGHTNESS_AUTO, isAuto)

    /**
     * 设置文字大小
     */
    var textSize: Int
        get() = SpUtil.getIntValue(SHARED_READ_TEXT_SIZE, ScreenUtil.spToPx(16))
        set(textSize) = SpUtil.setIntValue(SHARED_READ_TEXT_SIZE, textSize)

    /**
     * 是否默认文字大小
     */
    var isDefaultTextSize: Boolean
        get() = SpUtil.getBooleanValue(SHARED_READ_IS_TEXT_DEFAULT, false)
        set(isDefault) = SpUtil.setBooleanValue(SHARED_READ_IS_TEXT_DEFAULT, isDefault)

    /**
     * 页面类型
     */
    var pageMode: PageMode
        get() {
            val mode = SpUtil.getIntValue(SHARED_READ_PAGE_MODE, PageMode.SIMULATION.ordinal)
            return PageMode.values()[mode]
        }
        set(mode) = SpUtil.setIntValue(SHARED_READ_PAGE_MODE, mode.ordinal)

    /**
     * 页面风格
     */
    var pageStyle: PageStyle
        get() {
            val style = SpUtil.getIntValue(SHARED_READ_BG, PageStyle.BG_0.ordinal)
            return PageStyle.values()[style]
        }
        set(pageStyle) = SpUtil.setIntValue(SHARED_READ_BG, pageStyle.ordinal)

    /**
     * 是否暗夜模式
     */
    var isNightMode: Boolean
        get() = SpUtil.getBooleanValue(SHARED_READ_NIGHT_MODE, false)
        set(isNight) = SpUtil.setBooleanValue(SHARED_READ_NIGHT_MODE, isNight)

    /**
     * 音量键控制翻页
     */
    var isVolumeTurnPage: Boolean
        get() = SpUtil.getBooleanValue(SHARED_READ_VOLUME_TURN_PAGE, false)
        set(isTurn) = SpUtil.setBooleanValue(SHARED_READ_VOLUME_TURN_PAGE, isTurn)

    /**
     * 是否全屏
     */
    var isFullScreen: Boolean
        get() = SpUtil.getBooleanValue(SHARED_READ_FULL_SCREEN, false)
        set(isFullScreen) = SpUtil.setBooleanValue(SHARED_READ_FULL_SCREEN, isFullScreen)

    /**
     * 转化类型
     */
    var convertType: Int
        get() = SpUtil.getIntValue(SHARED_READ_CONVERT_TYPE, 1)
        set(convertType) = SpUtil.setIntValue(SHARED_READ_CONVERT_TYPE, convertType)
}

package com.resofy.music.extensions

import androidx.core.view.WindowInsetsCompat
import com.resofy.music.util.PreferenceUtil
import com.resofy.music.util.RetroUtil

fun WindowInsetsCompat?.getBottomInsets(): Int {
    return if (PreferenceUtil.isFullScreenMode) {
        return 0
    } else {
        this?.getInsets(WindowInsetsCompat.Type.systemBars())?.bottom ?: RetroUtil.navigationBarHeight
    }
}

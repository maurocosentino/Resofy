package com.resofy.music.util.theme

import android.content.Context
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDelegate
import com.resofy.music.R
import com.resofy.music.extensions.generalThemeValue
import com.resofy.music.util.PreferenceUtil
import com.resofy.music.util.theme.ThemeMode.*

@StyleRes
fun Context.getThemeResValue(): Int =
    if (PreferenceUtil.materialYou) {
        if (generalThemeValue == BLACK) R.style.Theme_Resofy_MD3_Black
        else R.style.Theme_Resofy_MD3
    } else {
        when (generalThemeValue) {
            LIGHT -> R.style.Theme_Resofy_Light
            DARK -> R.style.Theme_Resofy_Base
            BLACK -> R.style.Theme_Resofy_Black
            AUTO -> R.style.Theme_Resofy_FollowSystem
            GRUVBOX -> R.style.Theme_Resofy_Gruvbox
        }
    }

fun Context.getNightMode(): Int = when (generalThemeValue) {
    LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
    DARK -> AppCompatDelegate.MODE_NIGHT_YES
    GRUVBOX -> AppCompatDelegate.MODE_NIGHT_YES
    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
}
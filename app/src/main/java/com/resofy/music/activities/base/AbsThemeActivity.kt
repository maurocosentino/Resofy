package com.resofy.music.activities.base

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode
import androidx.core.os.LocaleListCompat
import code.name.monkey.appthemehelper.common.ATHToolbarActivity
import code.name.monkey.appthemehelper.util.VersionUtils
import com.resofy.music.R
import com.resofy.music.extensions.exitFullscreen
import com.resofy.music.extensions.hideStatusBar
import com.resofy.music.extensions.installSplitCompat
import com.resofy.music.extensions.maybeSetScreenOn
import com.resofy.music.extensions.maybeShowWhenLocked
import com.resofy.music.extensions.setEdgeToEdgeOrImmersive
import com.resofy.music.extensions.setImmersiveFullscreen
import com.resofy.music.extensions.setLightNavigationBarAuto
import com.resofy.music.extensions.setLightStatusBarAuto
import com.resofy.music.extensions.surfaceColor
import com.resofy.music.util.PreferenceUtil
import com.resofy.music.util.theme.getNightMode
import com.resofy.music.util.theme.getThemeResValue

abstract class AbsThemeActivity : ATHToolbarActivity(), Runnable {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        updateLocale()
        updateTheme()
        hideStatusBar()
        super.onCreate(savedInstanceState)
        setEdgeToEdgeOrImmersive()
        maybeSetScreenOn()
        maybeShowWhenLocked()
        setLightNavigationBarAuto()
        setLightStatusBarAuto(surfaceColor())
        if (VersionUtils.hasQ()) {
            window.decorView.isForceDarkAllowed = false
        }
    }

    private fun updateTheme() {
        setTheme(getThemeResValue())
        if (PreferenceUtil.materialYou) {
            setDefaultNightMode(getNightMode())
        }

        if (PreferenceUtil.isCustomFont) {
            setTheme(R.style.FontThemeOverlay)
        }
    }

    private fun updateLocale() {
        val localeCode = PreferenceUtil.languageCode
        if (localeCode == "auto") {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        } else {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(localeCode))
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideStatusBar()
            handler.removeCallbacks(this)
            handler.postDelayed(this, 300)
        } else {
            handler.removeCallbacks(this)
        }
    }

    override fun run() {
        setImmersiveFullscreen()
    }

    override fun onStop() {
        handler.removeCallbacks(this)
        super.onStop()
    }

    public override fun onDestroy() {
        super.onDestroy()
        exitFullscreen()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            handler.removeCallbacks(this)
            handler.postDelayed(this, 500)
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        installSplitCompat()
    }
}

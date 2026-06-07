/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.resofy.music.fragments.settings

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.preference.Preference
import androidx.preference.TwoStatePreference
import code.name.monkey.appthemehelper.ACCENT_COLORS
import code.name.monkey.appthemehelper.ACCENT_COLORS_SUB
import code.name.monkey.appthemehelper.ThemeStore
import code.name.monkey.appthemehelper.common.prefs.supportv7.ATEColorPreference
import code.name.monkey.appthemehelper.common.prefs.supportv7.ATEListPreference
import code.name.monkey.appthemehelper.common.prefs.supportv7.ATESwitchPreference
import code.name.monkey.appthemehelper.util.ColorUtil
import code.name.monkey.appthemehelper.util.VersionUtils
import com.resofy.music.*
import com.resofy.music.appshortcuts.DynamicShortcutManager
import com.resofy.music.extensions.installLanguageAndRecreate
import com.resofy.music.extensions.materialDialog
import com.resofy.music.fragments.NowPlayingScreen.*
import com.resofy.music.util.PreferenceUtil
import com.afollestad.materialdialogs.color.colorChooser
import com.google.android.material.color.DynamicColors

class AppearanceSettingsFragment : AbsSettingsFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    @SuppressLint("CheckResult")
    override fun invalidateSettings() {
        updateNowPlayingScreenSummary()

        // ── Tema de la app ──
        val generalTheme: Preference? = findPreference(GENERAL_THEME)
        generalTheme?.let {
            setSummary(it)
            it.setOnPreferenceChangeListener { _, newValue ->
                setSummary(it, newValue)
                ThemeStore.markChanged(requireContext())
                if (VersionUtils.hasNougatMR()) {
                    DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
                }
                restartActivity()
                true
            }
        }

        // ── Color de énfasis ──
        val accentColorPref: ATEColorPreference? = findPreference(ACCENT_COLOR)
        val accentColor = ThemeStore.accentColor(requireContext())
        accentColorPref?.setColor(accentColor, ColorUtil.darkenColor(accentColor))
        accentColorPref?.setOnPreferenceClickListener {
            materialDialog().show {
                colorChooser(
                    initialSelection = accentColor,
                    showAlphaSelector = false,
                    colors = ACCENT_COLORS,
                    subColors = ACCENT_COLORS_SUB,
                    allowCustomArgb = true
                ) { _, color ->
                    ThemeStore.editTheme(requireContext()).accentColor(color).commit()
                    if (VersionUtils.hasNougatMR())
                        DynamicShortcutManager(requireContext()).updateDynamicShortcuts()
                    restartActivity()
                }
            }
            true
        }

        // ── Color desaturado ──
        val desaturatedColor: ATESwitchPreference? = findPreference(DESATURATED_COLOR)
        desaturatedColor?.setOnPreferenceChangeListener { _, value ->
            val desaturated = value as Boolean
            ThemeStore.prefs(requireContext()).edit {
                putBoolean("desaturated_color", desaturated)
            }
            PreferenceUtil.isDesaturatedColor = desaturated
            restartActivity()
            true
        }

        // ── Fuente ──
        val customFont: ATESwitchPreference? = findPreference(CUSTOM_FONT)
        customFont?.setOnPreferenceChangeListener { _, _ ->
            restartActivity()
            true
        }

        // ── Wallpaper accent ──
        val wallpaperAccent: ATESwitchPreference? = findPreference(WALLPAPER_ACCENT)
        wallpaperAccent?.setOnPreferenceChangeListener { _, _ ->
            restartActivity()
            true
        }

        // ── Color adaptable ──
        val adaptiveColor: ATESwitchPreference? = findPreference(ADAPTIVE_COLOR_APP)
        adaptiveColor?.isEnabled =
            PreferenceUtil.nowPlayingScreen in listOf(Normal, Material, Flat)

        // ── Pantalla de bloqueo ──
        val albumArtOnLockscreen: ATESwitchPreference? = findPreference(ALBUM_ART_ON_LOCK_SCREEN)
        albumArtOnLockscreen?.isVisible = !VersionUtils.hasT()
        val blurredAlbumArt: ATESwitchPreference? = findPreference(BLURRED_ALBUM_ART)
        blurredAlbumArt?.isVisible = !VersionUtils.hasR()

        // ── Listeners de lista ──
        val tabTextMode: ATEListPreference? = findPreference(TAB_TEXT_MODE)
        tabTextMode?.setOnPreferenceChangeListener { prefs, newValue ->
            setSummary(prefs, newValue)
            true
        }
        val appBarMode: ATEListPreference? = findPreference(APPBAR_MODE)
        appBarMode?.setOnPreferenceChangeListener { _, _ ->
            restartActivity()
            true
        }
        val albumCoverTransform: Preference? = findPreference(ALBUM_COVER_TRANSFORM)
        albumCoverTransform?.setOnPreferenceChangeListener { prefs, newValue ->
            setSummary(prefs, newValue)
            true
        }
        val homeArtistStyle: ATEListPreference? = findPreference(HOME_ARTIST_GRID_STYLE)
        homeArtistStyle?.setOnPreferenceChangeListener { prefs, newValue ->
            setSummary(prefs, newValue)
            true
        }
        val homeAlbumStyle: ATEListPreference? = findPreference(HOME_ALBUM_GRID_STYLE)
        homeAlbumStyle?.setOnPreferenceChangeListener { prefs, newValue ->
            setSummary(prefs, newValue)
            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_appearance)

        requireContext().getSharedPreferences(
            "${requireContext().packageName}_preferences",
            android.content.Context.MODE_PRIVATE
        ).edit()
            .putBoolean(MATERIAL_YOU, false)
            .putBoolean(BLACK_THEME, false)
            .apply()

        val wallpaperAccent: ATESwitchPreference? = findPreference(WALLPAPER_ACCENT)
        wallpaperAccent?.isVisible = VersionUtils.hasOreoMR1() && !VersionUtils.hasS()
    }

    private fun updateNowPlayingScreenSummary() {
        val preference: Preference? = findPreference(NOW_PLAYING_SCREEN_ID)
        preference?.setSummary(PreferenceUtil.nowPlayingScreen.titleRes)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PreferenceUtil.registerOnSharedPreferenceChangedListener(this)

        PreferenceUtil.languageCode =
            AppCompatDelegate.getApplicationLocales().toLanguageTags().ifEmpty { "auto" }

        val languagePreference: Preference? = findPreference(LANGUAGE_NAME)
        languagePreference?.setOnPreferenceChangeListener { prefs, newValue ->
            setSummary(prefs, newValue)
            if (newValue as? String == "auto") {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
            } else {
                requireActivity().installLanguageAndRecreate(newValue.toString()) {
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(newValue as? String)
                    )
                }
            }
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PreferenceUtil.unregisterOnSharedPreferenceChangedListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        when (key) {
            NOW_PLAYING_SCREEN_ID -> updateNowPlayingScreenSummary()
            CIRCULAR_ALBUM_ART -> invalidateSettings()
        }
    }
}
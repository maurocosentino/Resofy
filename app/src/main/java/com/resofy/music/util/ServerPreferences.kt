package com.resofy.music.util

import android.content.Context
import androidx.core.content.edit

class ServerPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(
        "${context.packageName}_preferences",
        Context.MODE_PRIVATE
    )

    var serverUrl: String
        get() = prefs.getString("server_url", "") ?: ""
        set(value) { prefs.edit { putString("server_url", value) } }

    var username: String
        get() = prefs.getString("server_username", "") ?: ""
        set(value) { prefs.edit { putString("server_username", value) } }

    var password: String
        get() = prefs.getString("server_password", "") ?: ""
        set(value) { prefs.edit { putString("server_password", value) } }
}
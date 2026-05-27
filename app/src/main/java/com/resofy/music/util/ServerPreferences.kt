package com.resofy.music.util

import android.content.Context

class ServerPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(
        "${context.packageName}_preferences",
        Context.MODE_PRIVATE
    )

    val serverUrl: String
        get() = prefs.getString("server_url", "") ?: ""

    val username: String
        get() = prefs.getString("server_username", "") ?: ""

    val password: String
        get() = prefs.getString("server_password", "") ?: ""
}
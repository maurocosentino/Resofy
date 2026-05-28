package com.resofy.music.musicprovider

import android.content.Context
import com.resofy.music.musicprovider.local.LocalMusicProvider
import com.resofy.music.musicprovider.subsonic.SubsonicMusicProvider
import com.resofy.music.repository.RealRepository
import com.resofy.music.util.ServerPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProviderManager(
    private val context: Context,
    private val localRepository: RealRepository,
) {
    private val serverPrefs = ServerPreferences(context)

    private val _activeProviderType = MutableStateFlow(loadSavedProviderType())
    val activeProviderType: StateFlow<MusicProviderType> = _activeProviderType

    private var _activeProvider: MusicProvider = buildProvider(_activeProviderType.value)

    val activeProvider: MusicProvider get() = _activeProvider

    fun setProvider(type: MusicProviderType) {
        saveProviderType(type)
        _activeProvider = buildProvider(type)
        _activeProviderType.value = type
    }

    fun sync() {
        // Fuerza rebuild del provider para refrescar credenciales/config
        _activeProvider = buildProvider(_activeProviderType.value)
        // El reload real lo hace LibraryViewModel observando activeProviderType
        _activeProviderType.value = _activeProviderType.value
    }

    private fun buildProvider(type: MusicProviderType): MusicProvider {
        return when (type) {
            MusicProviderType.LOCAL -> LocalMusicProvider(localRepository)
            MusicProviderType.SUBSONIC -> SubsonicMusicProvider(
                baseUrl = serverPrefs.serverUrl,
                username = serverPrefs.username,
                password = serverPrefs.password,
            )
        }
    }

    private fun loadSavedProviderType(): MusicProviderType {
        val prefs = context.getSharedPreferences("provider_config", Context.MODE_PRIVATE)
        val saved = prefs.getString("active_provider", MusicProviderType.LOCAL.name)
        return try {
            MusicProviderType.valueOf(saved ?: MusicProviderType.LOCAL.name)
        } catch (e: Exception) {
            MusicProviderType.LOCAL
        }
    }

    private fun saveProviderType(type: MusicProviderType) {
        context.getSharedPreferences("provider_config", Context.MODE_PRIVATE)
            .edit()
            .putString("active_provider", type.name)
            .apply()
    }
}
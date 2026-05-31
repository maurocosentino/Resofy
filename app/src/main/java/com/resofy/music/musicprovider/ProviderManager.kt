package com.resofy.music.musicprovider

import ServerConfigEntity
import android.content.Context
import com.resofy.music.model.Song
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

    // Instancias persistentes — el caché vive aquí
    private val localProvider: LocalMusicProvider by lazy {
        LocalMusicProvider(localRepository)
    }

    private var subsonicProvider: SubsonicMusicProvider? = null

    private fun getOrCreateSubsonicProvider(): SubsonicMusicProvider {
        val current = subsonicProvider
        if (current != null) return current
        return SubsonicMusicProvider(
            baseUrl = serverPrefs.serverUrl,
            username = serverPrefs.username,
            password = serverPrefs.password,
        ).also { subsonicProvider = it }
    }

    val activeProvider: MusicProvider
        get() = when (_activeProviderType.value) {
            MusicProviderType.LOCAL -> localProvider
            MusicProviderType.SUBSONIC -> getOrCreateSubsonicProvider()
        }

    fun setProvider(type: MusicProviderType) {
        saveProviderType(type)
        _activeProviderType.value = type
    }

    suspend fun toggleStar(song: Song, isFavorite: Boolean) {
        activeProvider.toggleStar(song, isFavorite)
    }

    suspend fun scrobble(song: Song) {
        activeProvider.scrobble(song)
    }

    fun sync() {
        // Fuerza recreación del SubsonicProvider para refrescar credenciales
        subsonicProvider = SubsonicMusicProvider(
            baseUrl = serverPrefs.serverUrl,
            username = serverPrefs.username,
            password = serverPrefs.password,
        )
        // Dispara reload en LibraryViewModel
        _activeProviderType.value = _activeProviderType.value
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

    var activeServerId: Int
        get() = context.getSharedPreferences("provider_config", Context.MODE_PRIVATE)
            .getInt("active_server_id", -1)
        set(value) = context.getSharedPreferences("provider_config", Context.MODE_PRIVATE)
            .edit().putInt("active_server_id", value).apply()

    fun setActiveServer(server: ServerConfigEntity) {
        activeServerId = server.id
        subsonicProvider = SubsonicMusicProvider(
            baseUrl = server.url,
            username = server.username,
            password = server.password
        )
        _activeProviderType.value = MusicProviderType.SUBSONIC
        saveProviderType(MusicProviderType.SUBSONIC)
    }

    fun syncServer(server: ServerConfigEntity) {
        subsonicProvider = SubsonicMusicProvider(
            baseUrl = server.url,
            username = server.username,
            password = server.password
        )
        _activeProviderType.value = _activeProviderType.value
    }
}
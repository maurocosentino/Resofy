package com.resofy.music.musicprovider

import ServerConfigEntity
import android.content.Context
import com.resofy.music.model.Song
import com.resofy.music.musicprovider.local.LocalMusicProvider
import com.resofy.music.musicprovider.subsonic.SubsonicMusicProvider
import com.resofy.music.repository.RealRepository
import com.resofy.music.repository.ServerConfigRepository
import com.resofy.music.util.ServerPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProviderManager(
    private val context: Context,
    private val localRepository: RealRepository,
    private val serverConfigRepository: ServerConfigRepository,
) {

    init {
        // Al arrancar, si el provider activo es SUBSONIC y hay servidor guardado,
        // restaurar las credenciales desde Room
        if (loadSavedProviderType() == MusicProviderType.SUBSONIC) {
            val prefs = context.getSharedPreferences("provider_config", Context.MODE_PRIVATE)
            val savedUrl = prefs.getString("active_server_url", "") ?: ""
            if (savedUrl.isEmpty()) {
                // Las credenciales no están en provider_config — restaurar desde Room
                val savedId = prefs.getInt("active_server_id", -1)
                if (savedId != -1) {
                    kotlinx.coroutines.runBlocking {
                        val server = serverConfigRepository.getById(savedId)
                        if (server != null) {
                            prefs.edit()
                                .putString("active_server_url", server.url)
                                .putString("active_server_username", server.username)
                                .putString("active_server_password", server.password)
                                .apply()
                        }
                    }
                }
            }
        }
    }
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
        // Leer credenciales desde provider_config, no desde ServerPreferences
        val prefs = context.getSharedPreferences("provider_config", Context.MODE_PRIVATE)
        val url = prefs.getString("active_server_url", "") ?: ""
        val username = prefs.getString("active_server_username", "") ?: ""
        val password = prefs.getString("active_server_password", "") ?: ""
        return SubsonicMusicProvider(
            baseUrl = url,
            username = username,
            password = password,
            context = context,
        ).also { subsonicProvider = it }
    }

    fun setActiveServer(server: ServerConfigEntity) {
        activeServerId = server.id
        // Guardar credenciales en provider_config
        context.getSharedPreferences("provider_config", Context.MODE_PRIVATE)
            .edit()
            .putString("active_server_url", server.url)
            .putString("active_server_username", server.username)
            .putString("active_server_password", server.password)
            .apply()
        subsonicProvider = SubsonicMusicProvider(
            baseUrl = server.url,
            username = server.username,
            password = server.password,
            context = context,
        )
        _activeProviderType.value = MusicProviderType.SUBSONIC
        saveProviderType(MusicProviderType.SUBSONIC)
    }

    fun syncServer(server: ServerConfigEntity) {
        // Actualizar credenciales guardadas también
        context.getSharedPreferences("provider_config", Context.MODE_PRIVATE)
            .edit()
            .putString("active_server_url", server.url)
            .putString("active_server_username", server.username)
            .putString("active_server_password", server.password)
            .apply()
        subsonicProvider = SubsonicMusicProvider(
            baseUrl = server.url,
            username = server.username,
            password = server.password,
            context = context,
        )
        _activeProviderType.value = _activeProviderType.value
    }

//    fun sync() {
//        val prefs = context.getSharedPreferences("provider_config", Context.MODE_PRIVATE)
//        val url = prefs.getString("active_server_url", "") ?: ""
//        val username = prefs.getString("active_server_username", "") ?: ""
//        val password = prefs.getString("active_server_password", "") ?: ""
//        subsonicProvider = SubsonicMusicProvider(
//            baseUrl = url,
//            username = username,
//            password = password,
//            context = context,
//        )
//        _activeProviderType.value = _activeProviderType.value
//    }

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

}
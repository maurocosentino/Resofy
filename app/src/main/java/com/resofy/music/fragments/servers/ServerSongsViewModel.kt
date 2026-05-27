package com.resofy.music.fragments.servers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resofy.music.model.Song
import com.resofy.music.network.Result
import com.resofy.music.network.subsonic.SubsonicClient
import com.resofy.music.repository.SubsonicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ServerSongsState {
    object Loading : ServerSongsState()
    data class Success(val songs: List<Song>) : ServerSongsState()
    data class Error(val message: String) : ServerSongsState()
}

class ServerSongsViewModel(
    private val baseUrl: String,
    private val username: String,
    private val password: String,
) : ViewModel() {

    private val _state = MutableStateFlow<ServerSongsState>(ServerSongsState.Loading)
    val state: StateFlow<ServerSongsState> = _state

    private val repository: SubsonicRepository by lazy {
        SubsonicRepository(
            SubsonicClient.build(baseUrl, username, password),
            baseUrl,
            username,
            password
        )
    }

    init {
        if (baseUrl.isBlank()) {
            _state.value = ServerSongsState.Error("No hay servidor configurado. Andá a Settings → Servers.")
        } else {
            loadSongs()
        }
    }

    fun loadSongs() {
        if (baseUrl.isBlank()) {
            _state.value = ServerSongsState.Error("No hay servidor configurado.")
            return
        }
        viewModelScope.launch {
            _state.value = ServerSongsState.Loading
            when (val result = repository.getSongs()) {
                is Result.Success -> _state.value = ServerSongsState.Success(result.data)
                is Result.Error -> _state.value = ServerSongsState.Error(result.error.message ?: "Error desconocido")
                is Result.Loading -> { /* no-op */ }
            }
        }
    }
}
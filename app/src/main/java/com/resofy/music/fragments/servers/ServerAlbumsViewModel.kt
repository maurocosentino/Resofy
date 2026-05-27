package com.resofy.music.fragments.servers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resofy.music.model.Album
import com.resofy.music.network.Result
import com.resofy.music.network.subsonic.SubsonicClient
import com.resofy.music.repository.SubsonicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ServerAlbumsState {
    object Loading : ServerAlbumsState()
    data class Success(val albums: List<Album>) : ServerAlbumsState()
    data class Error(val message: String) : ServerAlbumsState()
}

class ServerAlbumsViewModel(
    baseUrl: String,
    username: String,
    password: String,
) : ViewModel() {

    private val _state = MutableStateFlow<ServerAlbumsState>(ServerAlbumsState.Loading)
    val state: StateFlow<ServerAlbumsState> = _state

    private val repository = SubsonicRepository(
        SubsonicClient.build(baseUrl, username, password),
        baseUrl, username, password
    )

    init {
        if (baseUrl.isNotEmpty()) {
            loadAlbums()
        } else {
            _state.value = ServerAlbumsState.Error("Configurá el servidor primero")
        }
    }

    fun loadAlbums() {
        viewModelScope.launch {
            android.util.Log.d("ServerAlbums", "loadAlbums started")
            _state.value = ServerAlbumsState.Loading
            try {
                val result = repository.getAlbums()
                when (result) {
                    is Result.Success -> _state.value = ServerAlbumsState.Success(result.data)
                    is Result.Error -> _state.value = ServerAlbumsState.Error(result.error.message ?: "Error")
                    is Result.Loading -> {}
                }
            } catch (e: Exception) {
                _state.value = ServerAlbumsState.Error(e.message ?: "Error")
            }
        }
    }
}
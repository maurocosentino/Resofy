package com.resofy.music.fragments.servers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resofy.music.model.Artist
import com.resofy.music.network.Result
import com.resofy.music.network.subsonic.SubsonicClient
import com.resofy.music.repository.SubsonicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ServerArtistsState {
    object Loading : ServerArtistsState()
    data class Success(val artists: List<Artist>) : ServerArtistsState()
    data class Error(val message: String) : ServerArtistsState()
}

class ServerArtistsViewModel(
    baseUrl: String,
    username: String,
    password: String,
) : ViewModel() {

    private val _state = MutableStateFlow<ServerArtistsState>(ServerArtistsState.Loading)
    val state: StateFlow<ServerArtistsState> = _state

    private val repository = SubsonicRepository(
        SubsonicClient.build(baseUrl, username, password),
        baseUrl, username, password
    )

    init {
        if (baseUrl.isNotEmpty()) loadArtists()
        else _state.value = ServerArtistsState.Error("Configurá el servidor primero")
    }

    fun loadArtists() {
        viewModelScope.launch {
            _state.value = ServerArtistsState.Loading
            when (val result = repository.getArtists()) {
                is Result.Success -> _state.value = ServerArtistsState.Success(result.data)
                is Result.Error -> _state.value = ServerArtistsState.Error(result.error.message ?: "Error")
                is Result.Loading -> {}
            }
        }
    }
}
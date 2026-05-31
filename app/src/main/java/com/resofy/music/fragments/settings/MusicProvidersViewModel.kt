package com.resofy.music.fragments.settings

import ServerConfigEntity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.resofy.music.repository.ServerConfigRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MusicProvidersViewModel(
    private val serverConfigRepository: ServerConfigRepository
) : ViewModel() {

    private val _servers = MutableStateFlow<List<ServerConfigEntity>>(emptyList())
    val servers: StateFlow<List<ServerConfigEntity>> = _servers

    private val _selectedServer = MutableStateFlow<ServerConfigEntity?>(null)
    val selectedServer: StateFlow<ServerConfigEntity?> = _selectedServer

    init {
        loadServers()
    }

    fun loadServers() {
        viewModelScope.launch {
            _servers.value = serverConfigRepository.getAll()
        }
    }

    fun selectServer(server: ServerConfigEntity) {
        _selectedServer.value = server
    }

    fun clearSelection() {
        _selectedServer.value = null
    }

    fun saveServer(name: String, url: String, username: String, password: String) {
        viewModelScope.launch {
            val current = _selectedServer.value
            if (current != null) {
                serverConfigRepository.update(current.id, name, url, username, password)
            } else {
                serverConfigRepository.insert(
                    ServerConfigEntity(name = name, url = url, username = username, password = password)
                )
            }
            loadServers()
            _selectedServer.value = null
        }
    }

    fun deleteServer(server: ServerConfigEntity) {
        viewModelScope.launch {
            serverConfigRepository.delete(server)
            if (_selectedServer.value?.id == server.id) {
                _selectedServer.value = null
            }
            loadServers()
        }
    }
}
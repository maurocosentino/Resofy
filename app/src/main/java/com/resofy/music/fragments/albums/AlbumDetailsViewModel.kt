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
package com.resofy.music.fragments.albums

import androidx.lifecycle.*
import com.resofy.music.interfaces.IMusicServiceEventListener
import com.resofy.music.model.Album
import com.resofy.music.model.Artist
import com.resofy.music.musicprovider.ProviderManager
import com.resofy.music.network.Result
import com.resofy.music.network.model.LastFmAlbum
import com.resofy.music.repository.RealRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class AlbumDetailsViewModel(
    private val repository: RealRepository,
    private val providerManager: ProviderManager,
    private val albumId: Long
) : ViewModel(), IMusicServiceEventListener {
    private val albumDetails = MutableLiveData<Album>()

    init {
        fetchAlbum()
    }

    private fun fetchAlbum() {
        viewModelScope.launch(IO) {
            val album = providerManager.activeProvider.albumById(albumId)
                ?: repository.albumByIdAsync(albumId)

            if (album != null && album != Album.empty) {
                // Cargar canciones reales del álbum
                val songs = providerManager.activeProvider.songsForAlbum(albumId)
                val albumWithSongs = if (songs.isNotEmpty()) {
                    Album(id = album.id, songs = songs)
                } else {
                    album
                }
                albumDetails.postValue(albumWithSongs)
            } else {
                albumDetails.postValue(Album.empty)
            }
        }
    }

    fun getAlbum(): LiveData<Album> = albumDetails

    fun getArtist(artistId: Long): LiveData<Artist> = liveData(IO) {
        try {
            val cachedArtist = providerManager.activeProvider.artistById(artistId)
            if (cachedArtist != null) {
                emit(cachedArtist)
                return@liveData
            }
            emit(repository.artistById(artistId))
        } catch (e: Exception) {
            emit(Artist.empty)
        }
    }

    fun getAlbumArtist(artistName: String): LiveData<Artist> = liveData(IO) {
        try {
            val cached = providerManager.activeProvider.cachedArtistByName(artistName)
            if (cached != null) {
                emit(cached)
                return@liveData
            }
            emit(repository.albumArtistByName(artistName))
        } catch (e: Exception) {
            emit(Artist.empty)
        }
    }

    fun getAlbumInfo(album: Album): LiveData<Result<LastFmAlbum>> = liveData(IO) {
        emit(Result.Loading)
        emit(repository.albumInfo(album.artistName, album.title))
    }

    fun getMoreAlbums(artist: Artist): LiveData<List<Album>> = liveData(IO) {
        artist.albums.filter { item -> item.id != albumId }.let { albums ->
            if (albums.isNotEmpty()) emit(albums)
        }
    }

    override fun onMediaStoreChanged() {
        fetchAlbum()
    }

    override fun onServiceConnected() {}
    override fun onServiceDisconnected() {}
    override fun onQueueChanged() {}
    override fun onPlayingMetaChanged() {}
    override fun onPlayStateChanged() {}
    override fun onRepeatModeChanged() {}
    override fun onShuffleModeChanged() {}
    override fun onFavoriteStateChanged() {}
}

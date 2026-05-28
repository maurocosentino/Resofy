package com.resofy.music.musicprovider.subsonic

import com.resofy.music.model.Album
import com.resofy.music.model.Artist
import com.resofy.music.model.Song
import com.resofy.music.musicprovider.MusicProvider
import com.resofy.music.network.Result
import com.resofy.music.network.subsonic.SubsonicClient
import com.resofy.music.repository.SubsonicRepository

class SubsonicMusicProvider(
    private val baseUrl: String,
    private val username: String,
    private val password: String,
) : MusicProvider {

    private val repository: SubsonicRepository by lazy {
        SubsonicRepository(
            SubsonicClient.build(baseUrl, username, password),
            baseUrl, username, password
        )
    }

    override suspend fun songs(): List<Song> {
        return when (val result = repository.getSongs()) {
            is Result.Success -> result.data
            else -> emptyList()
        }
    }

    override suspend fun albums(): List<Album> {
        return when (val result = repository.getAlbums()) {
            is Result.Success -> result.data
            else -> emptyList()
        }
    }

    override suspend fun artists(): List<Artist> {
        return when (val result = repository.getArtists()) {
            is Result.Success -> result.data
            else -> emptyList()
        }
    }

    override suspend fun shuffle(): List<Song> = songs().shuffled()
}
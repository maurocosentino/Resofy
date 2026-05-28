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

    private var cachedSongs: List<Song> = emptyList()
    private var cachedAlbums: List<Album> = emptyList()
    private var cachedArtists: List<Artist> = emptyList()

    private val repository: SubsonicRepository by lazy {
        require(baseUrl.isNotEmpty()) { "baseUrl vacío" }
        SubsonicRepository(
            SubsonicClient.build(baseUrl, username, password),
            baseUrl, username, password
        )
    }

    override suspend fun songs(): List<Song> {
        if (baseUrl.isEmpty()) return emptyList()
        return try {
            when (val result = repository.getSongs()) {
                is Result.Success -> result.data.also { cachedSongs = it }
                else -> emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun songsForAlbum(albumId: Long): List<Song> {
        if (baseUrl.isEmpty()) return emptyList()
        val album = cachedAlbums.find { it.id == albumId } ?: return emptyList()
        // Extraer Subsonic ID del campo data del dummy song
        val subsonicId = album.safeGetFirstSong().data
            .removePrefix("https://subsonic-album-id:")
        return try {
            when (val result = repository.getSongsForAlbum(subsonicId)) {
                is Result.Success -> result.data
                else -> emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    override suspend fun albums(): List<Album> {
        if (baseUrl.isEmpty()) return emptyList()
        return try {
            when (val result = repository.getAlbums()) {
                is Result.Success -> result.data.also { cachedAlbums = it }
                else -> emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun artists(): List<Artist> {
        if (baseUrl.isEmpty()) return emptyList()
        return try {
            when (val result = repository.getArtists()) {
                is Result.Success -> result.data.also { cachedArtists = it }
                else -> emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun shuffle(): List<Song> {
        if (baseUrl.isEmpty()) return emptyList()
        return songs().shuffled()
    }

    override suspend fun albumById(albumId: Long): Album? =
        cachedAlbums.find { it.id == albumId }

    override suspend fun artistById(artistId: Long): Artist? =
        cachedArtists.find { it.id == artistId }

    override fun cachedArtistByName(name: String): Artist? =
        cachedArtists.find { it.name == name }
}
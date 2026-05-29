package com.resofy.music.musicprovider.subsonic

import com.resofy.music.R
import com.resofy.music.RECENT_ALBUMS
import com.resofy.music.RECENT_ARTISTS
import com.resofy.music.TOP_ALBUMS
import com.resofy.music.TOP_ARTISTS
import com.resofy.music.model.Album
import com.resofy.music.model.Artist
import com.resofy.music.model.Home
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

    override suspend fun albumsForArtist(artistId: Long): List<Album> {
        if (baseUrl.isEmpty()) return emptyList()
        val artist = cachedArtists.find { it.id == artistId } ?: return emptyList()
        val subsonicId = artist.safeGetFirstAlbum().safeGetFirstSong().data
            .removePrefix("https://subsonic-artist-id:")
        return try {
            when (val result = repository.getAlbumsForArtist(subsonicId)) {
                is Result.Success -> {
                    // Cargar canciones reales para cada álbum
                    val albumsWithSongs = result.data.map { album ->
                        val albumSubsonicId = album.safeGetFirstSong().data
                            .removePrefix("https://subsonic-album-id:")
                        when (val songsResult = repository.getSongsForAlbum(albumSubsonicId)) {
                            is Result.Success -> Album(id = album.id, songs = songsResult.data)
                            else -> album
                        }
                    }
                    cachedAlbums = (cachedAlbums + albumsWithSongs).distinctBy { it.id }
                    albumsWithSongs
                }
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

    override suspend fun artistByName(name: String): Artist? {
        // Primero caché
        cachedArtists.find { it.name == name }?.let { return it }
        // Si no hay caché, cargar artistas
        if (baseUrl.isEmpty()) return null
        return try {
            artists().find { it.name == name }
        } catch (e: Exception) {
            null
        }
    }

//    override suspend fun suggestions(): List<Song> {
//        if (baseUrl.isEmpty()) return emptyList()
//        return songs().shuffled().take(9)
//    }

    override suspend fun homeSections(): List<Home> {
        if (baseUrl.isEmpty()) return emptyList()
        val homeSections = mutableListOf<Home>()

        // Intentar frequent, fallback a newest
        val topAlbums = when (val r = repository.getAlbumListByType("frequent", 10)) {
            is Result.Success -> r.data.ifEmpty {
                (repository.getAlbumListByType("newest", 10) as? Result.Success)?.data ?: emptyList()
            }
            else -> (repository.getAlbumListByType("newest", 10) as? Result.Success)?.data ?: emptyList()
        }
        if (topAlbums.isNotEmpty())
            homeSections.add(Home(topAlbums, TOP_ALBUMS, R.string.top_albums))

        // Intentar recent, fallback a alphabeticalByName
        val recentAlbums = when (val r = repository.getAlbumListByType("recent", 10)) {
            is Result.Success -> r.data.ifEmpty {
                (repository.getAlbumListByType("alphabeticalByName", 10) as? Result.Success)?.data ?: emptyList()
            }
            else -> (repository.getAlbumListByType("alphabeticalByName", 10) as? Result.Success)?.data ?: emptyList()
        }
        if (recentAlbums.isNotEmpty()) {
            homeSections.add(Home(recentAlbums, RECENT_ALBUMS, R.string.recent_albums))
            val recentArtistNames = recentAlbums.map { it.artistName }.distinct()
            val recentArtists = cachedArtists.filter { it.name in recentArtistNames }.take(5)
            if (recentArtists.isNotEmpty())
                homeSections.add(Home(recentArtists, RECENT_ARTISTS, R.string.recent_artists))
        }

        // Top artists desde top albums
        val topArtistNames = topAlbums.map { it.artistName }.distinct()
        val topArtists = cachedArtists.filter { it.name in topArtistNames }.take(5)
        if (topArtists.isNotEmpty())
            homeSections.add(Home(topArtists, TOP_ARTISTS, R.string.top_artists))

        return homeSections
    }

    override suspend fun suggestions(): List<Song> {
        if (baseUrl.isEmpty()) return emptyList()
        // Usar canciones del caché si están disponibles
        if (cachedSongs.isNotEmpty()) return cachedSongs.shuffled().take(9)
        return songs().shuffled().take(9)
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
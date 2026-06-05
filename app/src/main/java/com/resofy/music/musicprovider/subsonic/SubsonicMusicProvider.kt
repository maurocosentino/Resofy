package com.resofy.music.musicprovider.subsonic

import com.resofy.music.FAVOURITES
import com.resofy.music.R
import com.resofy.music.RECENT_ALBUMS
import com.resofy.music.RECENT_ARTISTS
import com.resofy.music.SUGGESTED_ARTISTS
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

    override suspend fun homeSections(): List<Home> {
        if (baseUrl.isEmpty()) return emptyList()
        val homeSections = mutableListOf<Home>()

        // Top albums
        val topAlbums = when (val r = repository.getAlbumListByType("frequent", 10)) {
            is Result.Success -> r.data.ifEmpty {
                (repository.getAlbumListByType("newest", 10) as? Result.Success)?.data ?: emptyList()
            }
            else -> (repository.getAlbumListByType("newest", 10) as? Result.Success)?.data ?: emptyList()
        }
        if (topAlbums.isNotEmpty())
            homeSections.add(Home(topAlbums, TOP_ALBUMS, R.string.top_albums))

        // Favoritos (starred)
        val favSongs = favoriteSongs()
        if (favSongs.isNotEmpty())
            homeSections.add(Home(favSongs, FAVOURITES, R.string.favorites))

        // Artistas sugeridos — 5 aleatorios del día
        val allArtists = cachedArtists.ifEmpty { artists() }
        if (allArtists.isNotEmpty()) {
            val seed = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
            val suggested = allArtists
                .shuffled(kotlin.random.Random(seed))
                .take(5)
            homeSections.add(Home(suggested, SUGGESTED_ARTISTS, R.string.suggested_artists))
        }

        return homeSections
    }
    private var cachedStarredSongs: List<Song> = emptyList()

    override suspend fun favoriteSongs(): List<Song> {
        if (baseUrl.isEmpty()) return emptyList()
        return try {
            when (val result = repository.getStarredSongs()) {
                is Result.Success -> result.data.also { cachedStarredSongs = it }
                else -> cachedStarredSongs
            }
        } catch (e: Exception) {
            cachedStarredSongs
        }
    }

    override suspend fun toggleStar(song: Song, isFavorite: Boolean) {
        val subsonicId = extractSubsonicId(song) ?: return
        if (isFavorite) {
            repository.starSong(subsonicId)
            cachedStarredSongs = cachedStarredSongs + song  // actualización inmediata
        } else {
            repository.unstarSong(subsonicId)
            cachedStarredSongs = cachedStarredSongs.filter { it.id != song.id }
        }
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

    override suspend fun albumsByType(type: Int): List<Album> {
        if (baseUrl.isEmpty()) return emptyList()
        return when (type) {
            TOP_ALBUMS -> when (val r = repository.getAlbumListByType("frequent", 50)) {
                is Result.Success -> r.data
                else -> emptyList()
            }
            RECENT_ALBUMS -> when (val r = repository.getAlbumListByType("recent", 50)) {
                is Result.Success -> r.data
                else -> emptyList()
            }
            else -> emptyList()
        }
    }

    override suspend fun artistsByType(type: Int): List<Artist> {
        if (baseUrl.isEmpty()) return emptyList()
        val albumType = when (type) {
            TOP_ARTISTS -> "frequent"
            RECENT_ARTISTS -> "recent"
            else -> return emptyList()
        }
        return try {
            when (val r = repository.getAlbumListByType(albumType, 50)) {
                is Result.Success -> {
                    val artistNames = r.data.map { it.artistName }.distinct()
                    cachedArtists.filter { it.name in artistNames }
                        .ifEmpty {
                            // Si no hay caché, cargar artistas
                            artists().filter { it.name in artistNames }
                        }
                }
                else -> emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun extractSubsonicId(song: Song): String? {
        return if (song.data.startsWith("http")) {
            // data = "http://host/rest/stream?id=XXXXX&..."
            song.data.substringAfter("id=").substringBefore("&")
        } else null
    }

    override suspend fun scrobble(song: Song) {
        val subsonicId = extractSubsonicId(song) ?: return
        repository.scrobbleSong(subsonicId)
    }

    override fun cachedArtistByName(name: String): Artist? =
        cachedArtists.find { it.name == name }
}
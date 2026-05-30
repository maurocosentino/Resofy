package com.resofy.music.musicprovider.local

import com.resofy.music.model.Album
import com.resofy.music.model.Artist
import com.resofy.music.model.Home
import com.resofy.music.model.Song
import com.resofy.music.musicprovider.MusicProvider
import com.resofy.music.repository.RealRepository
import com.resofy.music.TOP_ALBUMS
import com.resofy.music.RECENT_ALBUMS
import com.resofy.music.TOP_ARTISTS
import com.resofy.music.RECENT_ARTISTS

class LocalMusicProvider(
    private val repository: RealRepository
) : MusicProvider {

    override suspend fun songs(): List<Song> = repository.allSongs()

    override suspend fun albums(): List<Album> = repository.fetchAlbums()

    override suspend fun artists(): List<Artist> = repository.fetchArtists()

    override suspend fun shuffle(): List<Song> = repository.allSongs()
    override suspend fun albumById(albumId: Long): Album? =
        repository.albumByIdAsync(albumId)

    override suspend fun homeSections(): List<Home> = repository.homeSections()
    override suspend fun suggestions(): List<Song> = repository.suggestions()
    override suspend fun artistById(artistId: Long): Artist? =
        repository.artistById(artistId)

    override suspend fun artistByName(name: String): Artist? = null

    override suspend fun albumsForArtist(artistId: Long): List<Album> =
        repository.artistById(artistId).albums

    override suspend fun songsForAlbum(albumId: Long): List<Song> =
        repository.albumByIdAsync(albumId).songs

    override suspend fun albumsByType(type: Int): List<Album> = when (type) {
        TOP_ALBUMS -> repository.topAlbums()
        RECENT_ALBUMS -> repository.recentAlbums()
        else -> emptyList()
    }

    override suspend fun artistsByType(type: Int): List<Artist> = when (type) {
        TOP_ARTISTS -> repository.topArtists()
        RECENT_ARTISTS -> repository.recentArtists()
        else -> emptyList()
    }

    override fun cachedArtistByName(name: String): Artist? = null
}
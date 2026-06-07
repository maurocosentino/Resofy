package com.resofy.music.musicprovider

import com.resofy.music.model.Album
import com.resofy.music.model.Artist
import com.resofy.music.model.Home
import com.resofy.music.model.Song

interface MusicProvider {
    suspend fun songs(): List<Song>
    suspend fun albums(): List<Album>
    suspend fun artists(): List<Artist>
    suspend fun shuffle(): List<Song>
    suspend fun albumById(albumId: Long): Album?
    suspend fun artistById(artistId: Long): Artist?
    suspend fun artistByName(name: String): Artist?
    suspend fun albumsForArtist(artistId: Long): List<Album>
    suspend fun songsForAlbum(albumId: Long): List<Song>
    suspend fun homeSections(): List<Home>
    suspend fun favoriteSongs(): List<Song>
    suspend fun toggleStar(song: Song, isFavorite: Boolean) {}  // default vacío
    suspend fun scrobble(song: Song) {}  // default vacío
    suspend fun suggestions(): List<Song>
    fun cachedArtistByName(name: String): Artist?
    suspend fun albumsByType(type: Int): List<Album>
    suspend fun artistsByType(type: Int): List<Artist>
}
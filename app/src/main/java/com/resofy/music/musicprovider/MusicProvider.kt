package com.resofy.music.musicprovider

import com.resofy.music.model.Album
import com.resofy.music.model.Artist
import com.resofy.music.model.Song

interface MusicProvider {
    suspend fun songs(): List<Song>
    suspend fun albums(): List<Album>
    suspend fun artists(): List<Artist>
    suspend fun shuffle(): List<Song>
    suspend fun albumById(albumId: Long): Album?
    suspend fun artistById(artistId: Long): Artist?
    fun cachedArtistByName(name: String): Artist?
    suspend fun songsForAlbum(albumId: Long): List<Song>
}
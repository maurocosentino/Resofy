package com.resofy.music.musicprovider.local

import com.resofy.music.model.Album
import com.resofy.music.model.Artist
import com.resofy.music.model.Song
import com.resofy.music.musicprovider.MusicProvider
import com.resofy.music.repository.RealRepository

class LocalMusicProvider(
    private val repository: RealRepository
) : MusicProvider {

    override suspend fun songs(): List<Song> = repository.allSongs()

    override suspend fun albums(): List<Album> = repository.fetchAlbums()

    override suspend fun artists(): List<Artist> = repository.fetchArtists()

    override suspend fun shuffle(): List<Song> = repository.allSongs()
}
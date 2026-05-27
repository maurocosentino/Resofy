package com.resofy.music.repository

import com.resofy.music.model.Album
import com.resofy.music.model.Artist
import com.resofy.music.model.Song
import com.resofy.music.network.Result
import com.resofy.music.network.subsonic.SubsonicMapper.toAlbum
import com.resofy.music.network.subsonic.SubsonicMapper.toArtist
import com.resofy.music.network.subsonic.SubsonicMapper.toSong
import com.resofy.music.network.subsonic.SubsonicService

class SubsonicRepository(
    private val service: SubsonicService,
    private val baseUrl: String,
    private val username: String,
    private val password: String
) {
    suspend fun getSongs(): Result<List<Song>> {
        return try {
            val response = service.search(query = "", songCount = 500)
            val songs = response.response.searchResult3?.song
                ?.map { it.toSong(baseUrl, username, password) }
                ?: emptyList()
            Result.Success(songs)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getAlbums(): Result<List<Album>> {
        return try {
            val response = service.getAlbumList()
//            android.util.Log.d("Subsonic", "albums status=${response.response.status}")
//            android.util.Log.d("Subsonic", "albums error=${response.response.error}")
//            android.util.Log.d("Subsonic", "albums count=${response.response.albumList2?.album?.size}")
            val albums = response.response.albumList2?.album
                ?.map { it.toAlbum(baseUrl, username, password) }
                ?: emptyList()
            Result.Success(albums)
        } catch (e: Exception) {
            android.util.Log.e("Subsonic", "albums exception=${e.message}", e)
            Result.Error(e)
        }
    }

    suspend fun getArtists(): Result<List<Artist>> {
        return try {
            val response = service.getArtists()
            val artists = response.response.artists?.index
                ?.flatMap { it.artist ?: emptyList() }
                ?.map { it.toArtist() }
                ?: emptyList()
            Result.Success(artists)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun testConnection(): Result<String> {
        return try {
            val response = service.search(query = "", songCount = 1)
            if (response.response.status == "ok") {
                Result.Success(response.response.version)
            } else {
                val msg = response.response.error?.message ?: "Unknown error"
                Result.Error(Exception(msg))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
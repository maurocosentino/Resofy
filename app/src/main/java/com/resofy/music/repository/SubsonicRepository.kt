package com.resofy.music.repository

import com.resofy.music.model.Song
import com.resofy.music.network.subsonic.SubsonicMapper.toSong
import com.resofy.music.network.subsonic.SubsonicService
import com.resofy.music.network.Result

class SubsonicRepository(
    private val service: SubsonicService,
    private val baseUrl: String,
    private val username: String,
    private val password: String
) {
    suspend fun getSongs(): Result<List<Song>> {
        return try {
            val response = service.search(query = "", songCount = 500)
            android.util.Log.d("Subsonic", "status=${response.response.status}")
            android.util.Log.d("Subsonic", "songs=${response.response.searchResult3?.song?.size}")
            val songs = response.response.searchResult3?.song
                ?.map { it.toSong(baseUrl, username, password) }
                ?: emptyList()
            Result.Success(songs)
        } catch (e: Exception) {
            android.util.Log.e("Subsonic", "exception: ${e.message}", e)
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
            android.util.Log.e("Subsonic", "test exception: ${e.message}", e)
            Result.Error(e)
        }
    }
}
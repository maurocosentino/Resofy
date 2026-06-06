package com.resofy.music.musicprovider.subsonic

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.resofy.music.model.Album
import com.resofy.music.model.Artist
import com.resofy.music.model.Song
import java.io.File

class SubsonicCache(private val context: Context, private val serverKey: String) {

    private val gson = Gson()
    private val cacheDir get() = File(
        context.filesDir,
        "subsonic_cache/${serverKey.toByteArray().fold(0L) { acc, b -> acc * 31 + b }}"
    ).also { it.mkdirs() }
    private val songsFile get() = File(cacheDir, "songs.json")
    private val albumsFile get() = File(cacheDir, "albums.json")
    private val artistsFile get() = File(cacheDir, "artists.json")
    private val starredFile get() = File(cacheDir, "starred.json")

    fun saveSongs(songs: List<Song>) = saveToFile(songsFile, songs)
    fun saveAlbums(albums: List<Album>) = saveToFile(albumsFile, albums)
    fun saveArtists(artists: List<Artist>) = saveToFile(artistsFile, artists)
    fun saveStarred(songs: List<Song>) = saveToFile(starredFile, songs)

    fun loadSongs(): List<Song> = loadFromFile(songsFile, object : TypeToken<List<Song>>() {})
    fun loadAlbums(): List<Album> = loadFromFile(albumsFile, object : TypeToken<List<Album>>() {})
    fun loadArtists(): List<Artist> = loadFromFile(artistsFile, object : TypeToken<List<Artist>>() {})
    fun loadStarred(): List<Song> = loadFromFile(starredFile, object : TypeToken<List<Song>>() {})

    fun clear() = cacheDir.listFiles()?.forEach { it.delete() }

    private fun <T> saveToFile(file: File, data: T) {
        try {
            file.writeText(gson.toJson(data))
        } catch (e: Exception) {
            // Silencioso — el caché es best-effort
        }
    }

    private fun <T> loadFromFile(file: File, type: TypeToken<T>): T {
        return try {
            if (file.exists()) {
                gson.fromJson(file.readText(), type.type) ?: emptyDefaultFor(type)
            } else {
                emptyDefaultFor(type)
            }
        } catch (e: Exception) {
            emptyDefaultFor(type)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> emptyDefaultFor(type: TypeToken<T>): T = emptyList<Any>() as T
}
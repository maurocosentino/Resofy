package com.resofy.music.network.subsonic

import com.resofy.music.model.Song

object SubsonicMapper {

    /**
     * Construye la URL de stream que ExoPlayer va a reproducir directamente.
     * Va al campo Song.data — que es lo que MusicService usa para reproducir.
     */
    fun buildStreamUrl(
        baseUrl: String,
        songId: String,
        username: String,
        password: String
    ): String {
        // Subsonic acepta la contraseña como hex token para mayor seguridad
        // Por simplicidad inicial usamos u/p directo
        val cleanUrl = baseUrl.trimEnd('/')
        return "$cleanUrl/rest/stream" +
                "?id=$songId" +
                "&v=1.16.1" +
                "&c=resofy" +
                "&u=$username" +
                "&p=$password" +
                "&f=json"
    }

    fun buildCoverUrl(
        baseUrl: String,
        coverArtId: String?,
        username: String,
        password: String
    ): String? {
        coverArtId ?: return null
        val cleanUrl = baseUrl.trimEnd('/')
        return "$cleanUrl/rest/getCoverArt" +
                "?id=$coverArtId" +
                "&v=1.16.1" +
                "&c=resofy" +
                "&u=$username" +
                "&p=$password"
    }

    fun SubsonicSong.toSong(
        baseUrl: String,
        username: String,
        password: String
    ): Song {
        return Song(

            id = id.hashCode().toLong().let { if (it < 0) -it else it },
            title = title,
            trackNumber = track ?: 0,
            year = year ?: 0,
            duration = ((duration ?: 0) * 1000).toLong(), // Subsonic da segundos, Song espera ms
            data = buildStreamUrl(baseUrl, id, username, password), // ← la URL va aquí
            dateModified = 0L,
            albumId = albumId?.hashCode()?.toLong()?.let { if (it < 0) -it else it } ?: 0L,
            albumName = album ?: "",
            artistId = artistId?.hashCode()?.toLong()?.let { if (it < 0) -it else it } ?: 0L,
            artistName = artist ?: "",
            composer = null,
            albumArtist = null
        )
    }
}
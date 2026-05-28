package com.resofy.music.network.subsonic

import com.resofy.music.model.Album
import com.resofy.music.model.Artist
import com.resofy.music.model.Song
import kotlin.collections.mutableListOf

object SubsonicMapper {

    fun buildStreamUrl(baseUrl: String, songId: String, username: String, password: String): String {
        val cleanUrl = baseUrl.trimEnd('/')
        return "$cleanUrl/rest/stream?id=$songId&v=1.16.1&c=resofy&u=$username&p=$password&f=json"
    }

    fun buildCoverUrl(baseUrl: String, coverArtId: String?, username: String, password: String): String? {
        coverArtId ?: return null
        val cleanUrl = baseUrl.trimEnd('/')
        return "$cleanUrl/rest/getCoverArt?id=$coverArtId&v=1.16.1&c=resofy&u=$username&p=$password"
    }

    fun SubsonicSong.toSong(baseUrl: String, username: String, password: String): Song {
        val coverUrl = buildCoverUrl(baseUrl, coverArt, username, password) ?: ""
        return Song(
            id = id.hashCode().toLong().let { if (it < 0) -it else it },
            title = title,
            trackNumber = track ?: 0,
            year = year ?: 0,
            duration = ((duration ?: 0) * 1000).toLong(),
            data = buildStreamUrl(baseUrl, id, username, password),
            dateModified = 0L,
            albumId = albumId?.hashCode()?.toLong()?.let { if (it < 0) -it else it } ?: 0L,
            albumName = album ?: "",
            artistId = artistId?.hashCode()?.toLong()?.let { if (it < 0) -it else it } ?: 0L,
            artistName = artist ?: "",
            composer = null,
            albumArtist = coverUrl
        )
    }

    fun SubsonicAlbum.toAlbum(baseUrl: String, username: String, password: String): Album {
        val coverUrl = buildCoverUrl(baseUrl, coverArt, username, password) ?: ""
        val albumLongId = id.hashCode().toLong().let { if (it < 0) -it else it }
        val dummySong = Song(
            id = albumLongId,
            title = id,  //guarda el Subsonic ID temporalmente
            trackNumber = 0,
            year = year ?: 0,
            duration = 0L,
            data = "https://subsonic-album-id:$id",  //ID en data para recuperarlo
            dateModified = 0L,
            albumId = albumLongId,
            albumName = name,
            artistId = artistId?.hashCode()?.toLong()?.let { if (it < 0) -it else it } ?: 0L,
            artistName = artist ?: "",
            composer = coverUrl,
            albumArtist = artist ?: ""
        )
        return Album(id = albumLongId, songs = mutableListOf(dummySong))
    }

    fun SubsonicArtist.toArtist(): Artist {
        val artistLongId = id.hashCode().toLong().let { if (it < 0) -it else it }
        val dummyAlbum = Album(
            id = artistLongId,
            songs = listOf(
                Song(
                    id = artistLongId,
                    title = "",
                    trackNumber = 0,
                    year = 0,
                    duration = 0L,
                    data = "https://subsonic-artist-id:$id",
                    dateModified = 0L,
                    albumId = artistLongId,
                    albumName = "",
                    artistId = artistLongId,
                    artistName = name,
                    composer = null,
                    albumArtist = null
                )
            )
        )
        return Artist(
            artistName = name,
            albums = listOf(dummyAlbum)
        )
    }
}
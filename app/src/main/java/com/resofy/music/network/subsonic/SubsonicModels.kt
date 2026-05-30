package com.resofy.music.network.subsonic

import com.google.gson.annotations.SerializedName

data class SubsonicResponse(
    @SerializedName("subsonic-response") val response: SubsonicResponseBody
)

data class SubsonicResponseBody(
    val status: String,
    val version: String,
    val searchResult3: SearchResult3?,
    val albumList2: AlbumList2?,
    val artists: ArtistsResult?,
    val album: SubsonicAlbumDetail?,
    val artist: SubsonicArtistDetail?,
    val starred2: Starred2?,
    val error: SubsonicError?
)

data class Starred2(
    val song: List<SubsonicSong>?,
    val album: List<SubsonicAlbum>?,
    val artist: List<SubsonicArtist>?
)


data class SearchResult3(
    val song: List<SubsonicSong>?
)

data class SubsonicSong(
    val id: String,
    val title: String,
    val album: String?,
    val albumId: String?,
    val artist: String?,
    val artistId: String?,
    val track: Int?,
    val year: Int?,
    val duration: Int?,
    val coverArt: String?,
    val size: Long?
)

// Álbumes
data class AlbumList2(
    val album: List<SubsonicAlbum>?
)

data class SubsonicAlbum(
    val id: String,
    val name: String,
    val title: String?,
    val artist: String?,
    val artistId: String?,
    val coverArt: String?,
    val year: Int?
)

data class SubsonicAlbumDetail(
    val id: String,
    val name: String,
    val artist: String?,
    val song: List<SubsonicSong>?
)

// Artistas
data class ArtistsResult(
    val index: List<ArtistIndex>?
)
data class SubsonicArtistDetail(
    val id: String,
    val name: String,
    val album: List<SubsonicAlbum>?
)
data class ArtistIndex(
    val name: String,
    val artist: List<SubsonicArtist>?
)

data class SubsonicArtist(
    val id: String,
    val name: String,
    val albumCount: Int?
)

data class SubsonicError(
    val code: Int,
    val message: String
)
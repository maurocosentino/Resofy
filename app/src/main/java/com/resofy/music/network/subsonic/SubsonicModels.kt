package com.resofy.music.network.subsonic

import com.google.gson.annotations.SerializedName

data class SubsonicResponse(
    @SerializedName("subsonic-response") val response: SubsonicResponseBody
)

data class SubsonicResponseBody(
    val status: String,
    val version: String,
    val searchResult3: SearchResult3?,
    val error: SubsonicError?
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

data class SubsonicError(
    val code: Int,
    val message: String
)
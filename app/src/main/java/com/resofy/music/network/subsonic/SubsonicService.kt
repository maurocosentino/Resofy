package com.resofy.music.network.subsonic

import retrofit2.http.GET
import retrofit2.http.Query

interface SubsonicService {

    @GET("rest/search3")
    suspend fun search(
        @Query("query") query: String,
        @Query("songCount") songCount: Int = 50,
        @Query("f") format: String = "json"
    ): SubsonicResponse

    @GET("rest/getSongs")
    suspend fun getAllSongs(
        @Query("size") size: Int = 500,
        @Query("offset") offset: Int = 0,
        @Query("f") format: String = "json"
    ): SubsonicResponse
    @GET("rest/getAlbum")
    suspend fun getAlbum(
        @Query("id") albumId: String,
        @Query("f") format: String = "json"
    ): SubsonicResponse
    @GET("rest/getAlbumList2")
    suspend fun getAlbumList(
        @Query("type") type: String = "alphabeticalByName",
        @Query("size") size: Int = 500,
        @Query("f") format: String = "json"
    ): SubsonicResponse

    @GET("rest/getSongsByGenre")
    suspend fun getRandomSongs(
        @Query("size") size: Int = 20,
        @Query("f") format: String = "json"
    ): SubsonicResponse
    @GET("rest/getArtist")
    suspend fun getArtist(
        @Query("id") artistId: String,
        @Query("f") format: String = "json"
    ): SubsonicResponse
    @GET("rest/getArtists")
    suspend fun getArtists(
        @Query("f") format: String = "json"
    ): SubsonicResponse

    @GET("rest/getStarred2")
    suspend fun getStarred(
        @Query("f") format: String = "json"
    ): SubsonicResponse

    @GET("rest/star")
    suspend fun star(
        @Query("id") songId: String,
        @Query("f") format: String = "json"
    ): SubsonicResponse

    @GET("rest/unstar")
    suspend fun unstar(
        @Query("id") songId: String,
        @Query("f") format: String = "json"
    ): SubsonicResponse

    @GET("rest/scrobble")
    suspend fun scrobble(
        @Query("id") songId: String,
        @Query("submission") submission: Boolean = true,
        @Query("f") format: String = "json"
    ): SubsonicResponse
}
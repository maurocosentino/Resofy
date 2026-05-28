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
        @Query("size") size: Int = 100,
        @Query("f") format: String = "json"
    ): SubsonicResponse

    @GET("rest/getArtists")
    suspend fun getArtists(
        @Query("f") format: String = "json"
    ): SubsonicResponse

    // Este endpoint no se llama por Retrofit — se construye la URL y va directo a ExoPlayer
    // GET rest/stream?id={id}&v=1.16.1&c=resofy&u={user}&p={pass}&f=json
}
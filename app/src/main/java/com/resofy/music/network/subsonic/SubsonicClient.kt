package com.resofy.music.network.subsonic

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object SubsonicClient {

    fun build(baseUrl: String, username: String, password: String): SubsonicService {
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val newUrl = original.url.newBuilder()
                .addQueryParameter("u", username)
                .addQueryParameter("p", password)
                .addQueryParameter("v", "1.16.1")
                .addQueryParameter("c", "resofy")
                .build()
            chain.proceed(original.newBuilder().url(newUrl).build())
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl.trimEnd('/') + "/")
            .addConverterFactory(GsonConverterFactory.create())
            .callFactory { client.newCall(it) }
            .build()
            .create(SubsonicService::class.java)
    }
}
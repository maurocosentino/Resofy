package com.resofy.music.fragments.settings

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import com.resofy.music.R
import com.resofy.music.extensions.showToast
import com.resofy.music.network.Result
import com.resofy.music.network.subsonic.SubsonicClient
import com.resofy.music.repository.SubsonicRepository
import com.resofy.music.util.ServerPreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ServersSettingsFragment : AbsSettingsFragment() {

    private val serverPrefs by lazy { ServerPreferences(requireContext()) }
    private var testJob: Job? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_servers)
    }

    override fun invalidateSettings() {
        findPreference<Preference>("server_url")?.apply {
            summary = serverPrefs.serverUrl.ifEmpty { "No configurado" }
        }

        findPreference<Preference>("server_test")?.setOnPreferenceClickListener {
            testConnection()
            true
        }

        findPreference<Preference>("server_browse")?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_serversSettingsFragment_to_serverSongsFragment)
            true
        }
    }

    private fun testConnection() {
        val url = serverPrefs.serverUrl
        val user = serverPrefs.username
        val pass = serverPrefs.password

        if (url.isEmpty()) {
            showToast("Configurá el servidor primero")
            return
        }

        testJob?.cancel()
        testJob = viewLifecycleOwner.lifecycleScope.launch {
            val repo = SubsonicRepository(SubsonicClient.build(url, user, pass), url, user, pass)
            when (val result = repo.testConnection()) {
                is Result.Success -> showToast("✓ Conectado — API v${result.data}")
                is Result.Error -> showToast("✗ Error: ${result.error.message}")
                is Result.Loading -> { /* no-op */ }
            }
        }
    }

//    private fun buildService(url: String, user: String, pass: String): SubsonicService {
//        val authInterceptor = Interceptor { chain ->
//            val original = chain.request()
//            val newUrl = original.url.newBuilder()
//                .addQueryParameter("u", user)
//                .addQueryParameter("p", pass)
//                .addQueryParameter("v", "1.16.1")
//                .addQueryParameter("c", "resofy")
//                .build()
//            chain.proceed(original.newBuilder().url(newUrl).build())
//        }
//
//        val client = OkHttpClient.Builder()
//            .addInterceptor(authInterceptor)
//            .connectTimeout(10, TimeUnit.SECONDS)
//            .readTimeout(30, TimeUnit.SECONDS)
//            .build()
//
//        return Retrofit.Builder()
//            .baseUrl(url.trimEnd('/') + "/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .callFactory { client.newCall(it) }
//            .build()
//            .create(SubsonicService::class.java)
//    }
}
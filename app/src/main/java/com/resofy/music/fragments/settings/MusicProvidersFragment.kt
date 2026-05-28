package com.resofy.music.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.resofy.music.R
import com.resofy.music.databinding.FragmentMusicProvidersBinding
import com.resofy.music.extensions.showToast
import com.resofy.music.musicprovider.MusicProviderType
import com.resofy.music.musicprovider.ProviderManager
import com.resofy.music.network.subsonic.SubsonicClient
import com.resofy.music.repository.SubsonicRepository
import com.resofy.music.util.ServerPreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MusicProvidersFragment : Fragment() {

    private var _binding: FragmentMusicProvidersBinding? = null
    private val binding get() = _binding!!

    private val providerManager: ProviderManager by inject()
    private val serverPrefs by lazy { ServerPreferences(requireContext()) }
    private var testJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMusicProvidersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupProviderToggle()
        setupServerConfig()
        setupButtons()
    }

    private fun setupProviderToggle() {
        // Setear estado inicial
        val currentType = providerManager.activeProviderType.value
        binding.providerToggleGroup.check(
            if (currentType == MusicProviderType.LOCAL) R.id.btnLocal else R.id.btnSubsonic
        )
        updateUI(currentType)

        binding.providerToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val type = if (checkedId == R.id.btnLocal) {
                MusicProviderType.LOCAL
            } else {
                MusicProviderType.SUBSONIC
            }
            providerManager.setProvider(type)
            updateUI(type)
        }
    }

    private fun updateUI(type: MusicProviderType) {
        val isSubsonic = type == MusicProviderType.SUBSONIC
        binding.serverConfigCard.isVisible = isSubsonic
        binding.providerStatus.text = if (isSubsonic) {
            val url = serverPrefs.serverUrl
            if (url.isEmpty()) "Sin servidor configurado" else url
        } else {
            "Biblioteca local"
        }
    }

    private fun setupServerConfig() {
        binding.urlInput.setText(serverPrefs.serverUrl)
        binding.usernameInput.setText(serverPrefs.username)
        binding.passwordInput.setText(serverPrefs.password)
    }

    private fun setupButtons() {
        binding.btnSaveServer.setOnClickListener {
            saveServerConfig()
        }

        binding.btnTestConnection.setOnClickListener {
            testConnection()
        }

        binding.btnSync.setOnClickListener {
            saveServerConfig()
            providerManager.sync()
            showToast("Sincronizando biblioteca...")
        }
    }

    private fun saveServerConfig() {
        serverPrefs.serverUrl = binding.urlInput.text.toString().trim()
        serverPrefs.username = binding.usernameInput.text.toString().trim()
        serverPrefs.password = binding.passwordInput.text.toString().trim()
        updateUI(providerManager.activeProviderType.value)
        showToast("Configuración guardada")
    }

    private fun testConnection() {
        val url = binding.urlInput.text.toString().trim()
        val user = binding.usernameInput.text.toString().trim()
        val pass = binding.passwordInput.text.toString().trim()

        if (url.isEmpty()) {
            showToast("Ingresá la URL del servidor")
            return
        }

        testJob?.cancel()
        testJob = viewLifecycleOwner.lifecycleScope.launch {
            val repo = SubsonicRepository(
                SubsonicClient.build(url, user, pass),
                url, user, pass
            )
            when (val result = repo.testConnection()) {
                is com.resofy.music.network.Result.Success ->
                    showToast("✓ Conectado — API v${result.data}")
                is com.resofy.music.network.Result.Error ->
                    showToast("✗ Error: ${result.error.message}")
                is com.resofy.music.network.Result.Loading -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
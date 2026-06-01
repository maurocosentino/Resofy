package com.resofy.music.fragments.settings

import ServerConfigEntity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.resofy.music.R
import com.resofy.music.adapter.ServerConfigAdapter
import com.resofy.music.databinding.FragmentMusicProvidersBinding
import com.resofy.music.extensions.showToast
import com.resofy.music.musicprovider.MusicProviderType
import com.resofy.music.musicprovider.ProviderManager
import com.resofy.music.network.Result
import com.resofy.music.network.subsonic.SubsonicClient
import com.resofy.music.repository.SubsonicRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MusicProvidersFragment : Fragment() {

    private var _binding: FragmentMusicProvidersBinding? = null
    private val binding get() = _binding!!

    private val providerManager: ProviderManager by inject()
    private val viewModel: MusicProvidersViewModel by viewModel()
    private var testJob: Job? = null
    private var isEditing = false

    private lateinit var serverAdapter: ServerConfigAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMusicProvidersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupProviderToggle()
        setupServerList()
        setupServerForm()
        observeViewModel()
    }

    private fun setupProviderToggle() {
        val currentType = providerManager.activeProviderType.value
        binding.providerToggleGroup.check(
            if (currentType == MusicProviderType.LOCAL) R.id.btnLocal else R.id.btnSubsonic
        )
        updateProviderUI(currentType)

        binding.providerToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val type = if (checkedId == R.id.btnLocal) MusicProviderType.LOCAL else MusicProviderType.SUBSONIC
            providerManager.setProvider(type)
            updateProviderUI(type)
        }
    }

    private fun updateProviderUI(type: MusicProviderType) {
        val isSubsonic = type == MusicProviderType.SUBSONIC
        binding.serverListSection.isVisible = isSubsonic
        binding.providerStatus.text = if (isSubsonic) "Subsonic / Navidrome" else getString(R.string.local_library)
    }

    private fun setupServerList() {
        serverAdapter = ServerConfigAdapter(
            servers = emptyList(),
            activeServerId = providerManager.activeServerId,
            onSelect = { server ->
                providerManager.setActiveServer(server)
                serverAdapter.updateServers(viewModel.servers.value, server.id)
                showToast(getString(R.string.server_active, server.name.ifEmpty { server.url }))
            },
            onSync = { server ->
                providerManager.syncServer(server)
                showToast(getString(R.string.syncing_server, server.name.ifEmpty { server.url }))
            },
            onEdit = { server ->
                showServerForm(server)
            },
            onDelete = { server ->
                viewModel.deleteServer(server)
                if (providerManager.activeServerId == server.id) {
                    providerManager.activeServerId = -1
                }
            }
        )

        binding.recyclerServers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = serverAdapter
        }

        binding.btnAddServer.setOnClickListener {
            showServerForm(null)
        }
    }

    private fun showServerForm(server: ServerConfigEntity?) {
        isEditing = server != null
        binding.serverFormCard.isVisible = true
        binding.etServerName.setText(server?.name ?: "")
        binding.etServerUrl.setText(server?.url ?: "")
        binding.etServerUsername.setText(server?.username ?: "")
        binding.etServerPassword.setText(server?.password ?: "")
        if (server != null) viewModel.selectServer(server) else viewModel.clearSelection()
    }

    private fun setupServerForm() {
        binding.btnSaveServer.setOnClickListener {
            val name = binding.etServerName.text.toString().trim()
            val url = binding.etServerUrl.text.toString().trim()
            val user = binding.etServerUsername.text.toString().trim()
            val pass = binding.etServerPassword.text.toString().trim()

            if (url.isEmpty()) {
                showToast(getString(R.string.enter_server_url))
                return@setOnClickListener
            }
            viewModel.saveServer(name, url, user, pass)
            binding.serverFormCard.isVisible = false
            showToast(getString(if (isEditing) R.string.server_updated else R.string.server_added))
        }

        binding.btnTestConnection.setOnClickListener {
            val url = binding.etServerUrl.text.toString().trim()
            val user = binding.etServerUsername.text.toString().trim()
            val pass = binding.etServerPassword.text.toString().trim()
            if (url.isEmpty()) { showToast(getString(R.string.enter_server_url)); return@setOnClickListener }
            testJob?.cancel()
            testJob = viewLifecycleOwner.lifecycleScope.launch {
                val repo = SubsonicRepository(SubsonicClient.build(url, user, pass), url, user, pass)
                when (val result = repo.testConnection()) {
                    is Result.Success -> showToast(getString(R.string.connection_ok, result.data))
                    is Result.Error -> showToast(getString(R.string.connection_error, result.error.message))
                    is Result.Loading -> {}
                }
            }
        }

        binding.btnCancelForm.setOnClickListener {
            binding.serverFormCard.isVisible = false
            viewModel.clearSelection()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.servers.collect { servers ->
                    serverAdapter.updateServers(servers, providerManager.activeServerId)
                    binding.emptyServers.isVisible = servers.isEmpty()
                    binding.recyclerServers.isVisible = servers.isNotEmpty()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
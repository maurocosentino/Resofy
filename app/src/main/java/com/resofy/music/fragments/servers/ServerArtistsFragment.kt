package com.resofy.music.fragments.servers

import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.resofy.music.R
import com.resofy.music.adapter.artist.ArtistAdapter
import com.resofy.music.interfaces.IArtistClickListener
import com.resofy.music.util.ServerPreferences
import kotlinx.coroutines.launch

class ServerArtistsFragment : Fragment(), IArtistClickListener {

    private lateinit var viewModel: ServerArtistsViewModel
    private lateinit var adapter: ArtistAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView

    override fun onArtist(artistId: Long, view: View) {
        // futuro: navegar a detalle del artista remoto
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_server_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        emptyText = view.findViewById(R.id.emptyText)

        val prefs = ServerPreferences(requireContext())

        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ServerArtistsViewModel(prefs.serverUrl, prefs.username, prefs.password) as T
            }
        })[ServerArtistsViewModel::class.java]

        adapter = ArtistAdapter(requireActivity(), mutableListOf(), R.layout.item_list, this)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is ServerArtistsState.Loading -> {
                            progressBar.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                            emptyText.visibility = View.GONE
                        }
                        is ServerArtistsState.Success -> {
                            progressBar.visibility = View.GONE
                            if (state.artists.isEmpty()) {
                                emptyText.visibility = View.VISIBLE
                            } else {
                                recyclerView.visibility = View.VISIBLE
                                adapter.swapDataSet(state.artists)
                            }
                        }
                        is ServerArtistsState.Error -> {
                            progressBar.visibility = View.GONE
                            emptyText.visibility = View.VISIBLE
                            emptyText.text = state.message
                        }
                    }
                }
            }
        }
    }
}
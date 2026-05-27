package com.resofy.music.fragments.servers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.resofy.music.R
import com.resofy.music.adapter.song.SongAdapter
import com.resofy.music.util.ServerPreferences
import kotlinx.coroutines.launch

class ServerSongsFragment : Fragment() {

    private lateinit var viewModel: ServerSongsViewModel
    private lateinit var adapter: SongAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
                return ServerSongsViewModel(prefs.serverUrl, prefs.username, prefs.password) as T
            }
        })[ServerSongsViewModel::class.java]

        adapter = SongAdapter(requireActivity(), mutableListOf(), R.layout.item_list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is ServerSongsState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                        emptyText.visibility = View.GONE
                    }
                    is ServerSongsState.Success -> {
                        progressBar.visibility = View.GONE
                        if (state.songs.isEmpty()) {
                            emptyText.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        } else {
                            recyclerView.visibility = View.VISIBLE
                            emptyText.visibility = View.GONE
                            adapter.swapDataSet(state.songs)
                        }
                    }
                    is ServerSongsState.Error -> {
                        progressBar.visibility = View.GONE
                        recyclerView.visibility = View.GONE
                        emptyText.visibility = View.VISIBLE
                        emptyText.text = state.message
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance() = ServerSongsFragment()
    }
}
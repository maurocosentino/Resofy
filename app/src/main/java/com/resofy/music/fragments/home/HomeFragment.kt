/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.resofy.music.fragments.home

import android.os.Bundle
import android.view.*
import android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.core.text.parseAsHtml
import androidx.core.view.doOnLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import code.name.monkey.appthemehelper.common.ATHToolbarActivity
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper
import com.resofy.music.*
import com.resofy.music.adapter.HomeAdapter
import com.resofy.music.databinding.FragmentHomeBinding
import com.resofy.music.dialogs.CreatePlaylistDialog
import com.resofy.music.dialogs.ImportPlaylistDialog
import com.resofy.music.extensions.accentColor
import com.resofy.music.extensions.dip
import com.resofy.music.extensions.elevatedAccentColor
import com.resofy.music.extensions.setUpMediaRouteButton
import com.resofy.music.fragments.ReloadType
import com.resofy.music.fragments.base.AbsMainActivityFragment
import com.resofy.music.glide.RetroGlideExtension
import com.resofy.music.glide.RetroGlideExtension.songCoverOptions
import com.resofy.music.helper.MusicPlayerRemote
import com.resofy.music.interfaces.IScrollHelper
import com.resofy.music.model.Song
import com.resofy.music.util.PreferenceUtil
import com.resofy.music.util.PreferenceUtil.userName
import com.bumptech.glide.Glide
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import com.resofy.music.musicprovider.MusicProviderType
import com.resofy.music.musicprovider.ProviderManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class HomeFragment :
    AbsMainActivityFragment(R.layout.fragment_home), IScrollHelper {

    private val providerManager: ProviderManager by inject()
    private var _binding: HomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val homeBinding = FragmentHomeBinding.bind(view)
        _binding = HomeBinding(homeBinding)
        mainActivity.setSupportActionBar(binding.toolbar)
        mainActivity.supportActionBar?.title = null
        setupListeners()

        viewLifecycleOwner.lifecycleScope.launch {
            providerManager.activeProviderType.collect { type ->
                val isLocal = type == MusicProviderType.LOCAL
                if (isLocal) {
                    binding.absPlaylists.root.visibility = View.VISIBLE
                } else {
                    binding.history.visibility = View.GONE
                    binding.lastAdded.visibility = View.GONE
                    binding.topPlayed.visibility = View.GONE
                    binding.absPlaylists.root.visibility = View.GONE
                }
            }
        }

        enterTransition = MaterialFadeThrough().addTarget(binding.contentContainer)
        reenterTransition = MaterialFadeThrough().addTarget(binding.contentContainer)

        checkForMargins()

        val homeAdapter = HomeAdapter(mainActivity)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(mainActivity)
            adapter = homeAdapter
        }
        libraryViewModel.getSuggestions().observe(viewLifecycleOwner) {
            loadSuggestions(it)
        }
        libraryViewModel.getHome().observe(viewLifecycleOwner) {
            homeAdapter.swapData(it)
        }

        setupTitle()
        colorButtons()
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        view.doOnLayout {
            adjustPlaylistButtons()
        }
    }

    private fun adjustPlaylistButtons() {
        val buttons =
            listOf(binding.history, binding.lastAdded, binding.topPlayed, binding.actionShuffle)
        buttons.maxOf { it.lineCount }.let { maxLineCount ->
            buttons.forEach { button ->
                button.setLines(maxLineCount)
            }
        }
    }

    private fun setupListeners() {
        binding.lastAdded.setOnClickListener {
            findNavController().navigate(
                R.id.detailListFragment,
                bundleOf(EXTRA_PLAYLIST_TYPE to LAST_ADDED_PLAYLIST)
            )
            setSharedAxisYTransitions()
        }

        binding.topPlayed.setOnClickListener {
            findNavController().navigate(
                R.id.detailListFragment,
                bundleOf(EXTRA_PLAYLIST_TYPE to TOP_PLAYED_PLAYLIST)
            )
            setSharedAxisYTransitions()
        }

        binding.actionShuffle.setOnClickListener {
            libraryViewModel.shuffleSongs()
        }

        binding.history.setOnClickListener {
            findNavController().navigate(
                R.id.detailListFragment,
                bundleOf(EXTRA_PLAYLIST_TYPE to HISTORY_PLAYLIST)
            )
            setSharedAxisYTransitions()
        }

        binding.suggestions.refreshButton.setOnClickListener {
            libraryViewModel.forceReload(ReloadType.Suggestions)
        }
    }

    private fun setupTitle() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_search, null, navOptions)
        }
        val hexColor = String.format("#%06X", 0xFFFFFF and accentColor())
        val appName = "Resofy <font color=$hexColor>Music</font>".parseAsHtml()
        binding.appBarLayout.title = appName
    }

    fun colorButtons() {
        binding.history.elevatedAccentColor()
        binding.lastAdded.elevatedAccentColor()
        binding.topPlayed.elevatedAccentColor()
        binding.actionShuffle.elevatedAccentColor()
    }

    private fun checkForMargins() {
        if (mainActivity.isBottomNavVisible) {
            binding.recyclerView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = dip(R.dimen.bottom_nav_height)
            }
        }
    }

    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        menu.removeItem(R.id.action_grid_size)
        menu.removeItem(R.id.action_layout_type)
        menu.removeItem(R.id.action_sort_order)
        menu.findItem(R.id.action_settings).setShowAsAction(SHOW_AS_ACTION_IF_ROOM)
        ToolbarContentTintHelper.handleOnCreateOptionsMenu(
            requireContext(),
            binding.toolbar,
            menu,
            ATHToolbarActivity.getToolbarBackgroundColor(binding.toolbar)
        )
        requireContext().setUpMediaRouteButton(menu)
    }

    override fun scrollToTop() {
        binding.container.scrollTo(0, 0)
        binding.appBarLayout.setExpanded(true)
    }

    fun setSharedAxisXTransitions() {
        exitTransition =
            MaterialSharedAxis(MaterialSharedAxis.X, true).addTarget(CoordinatorLayout::class.java)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
    }

    private fun setSharedAxisYTransitions() {
        exitTransition =
            MaterialSharedAxis(MaterialSharedAxis.Y, true).addTarget(CoordinatorLayout::class.java)
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, false)
    }

    private fun loadSuggestions(songs: List<Song>) {
        if (!PreferenceUtil.homeSuggestions || songs.isEmpty()) {
            binding.suggestions.root.isVisible = false
            return
        }

        // Container (whole item row) + image for Glide + title + artist
        val songViews = listOf(
            listOf(binding.suggestions.song1, binding.suggestions.image1, binding.suggestions.title1, binding.suggestions.artist1),
            listOf(binding.suggestions.song2, binding.suggestions.image2, binding.suggestions.title2, binding.suggestions.artist2),
            listOf(binding.suggestions.song3, binding.suggestions.image3, binding.suggestions.title3, binding.suggestions.artist3),
            listOf(binding.suggestions.song4, binding.suggestions.image4, binding.suggestions.title4, binding.suggestions.artist4),
            listOf(binding.suggestions.song5, binding.suggestions.image5, binding.suggestions.title5, binding.suggestions.artist5),
            listOf(binding.suggestions.song6, binding.suggestions.image6, binding.suggestions.title6, binding.suggestions.artist6),
            listOf(binding.suggestions.song7, binding.suggestions.image7, binding.suggestions.title7, binding.suggestions.artist7),
            listOf(binding.suggestions.song8, binding.suggestions.image8, binding.suggestions.title8, binding.suggestions.artist8),
        )

        // Shuffle button — plays all 8 songs
        binding.suggestions.shuffleButton.setOnClickListener {
            it.isClickable = false
            it.postDelayed({ it.isClickable = true }, 500)
            MusicPlayerRemote.playNext(songs.subList(0, 8))
            if (!MusicPlayerRemote.isPlaying) {
                MusicPlayerRemote.playNextSong()
            }
        }

        songViews.forEachIndexed { index, views ->
            val container = views[0]  // song1…song8 — the clickable row
            val imageView = views[1] as androidx.appcompat.widget.AppCompatImageView
            val titleView = views[2] as com.google.android.material.textview.MaterialTextView
            val artistView = views[3] as com.google.android.material.textview.MaterialTextView
            val song = songs[index]

            titleView.text = song.title
            artistView.text = song.artistName

            // Click on the whole item row, not just the image
            container.setOnClickListener {
                it.isClickable = false
                it.postDelayed({ it.isClickable = true }, 500)
                MusicPlayerRemote.playNext(song)
                if (!MusicPlayerRemote.isPlaying) {
                    MusicPlayerRemote.playNextSong()
                }
            }

            Glide.with(this)
                .load(RetroGlideExtension.getSongModel(song))
                .songCoverOptions(song)
                .into(imageView)
        }
    }

    companion object {
        const val TAG: String = "BannerHomeFragment"

        @JvmStatic
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> findNavController().navigate(
                R.id.settings_fragment,
                null,
                navOptions
            )
            R.id.action_import_playlist -> ImportPlaylistDialog().show(
                childFragmentManager,
                "ImportPlaylist"
            )
            R.id.action_add_to_playlist -> CreatePlaylistDialog.create(emptyList()).show(
                childFragmentManager,
                "ShowCreatePlaylistDialog"
            )
        }
        return false
    }

    override fun onPrepareMenu(menu: Menu) {
        super.onPrepareMenu(menu)
        ToolbarContentTintHelper.handleOnPrepareOptionsMenu(requireActivity(), binding.toolbar)
    }

    override fun onResume() {
        super.onResume()
        checkForMargins()
        libraryViewModel.forceReload(ReloadType.HomeSections)
        exitTransition = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
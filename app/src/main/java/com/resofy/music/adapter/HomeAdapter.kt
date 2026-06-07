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
package com.resofy.music.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.findFragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.resofy.music.*
import com.resofy.music.adapter.album.AlbumAdapter
import com.resofy.music.adapter.artist.ArtistAdapter
import com.resofy.music.adapter.song.SongAdapter
import com.resofy.music.fragments.home.HomeFragment
import com.resofy.music.interfaces.IAlbumClickListener
import com.resofy.music.interfaces.IArtistClickListener
import com.resofy.music.model.Album
import com.resofy.music.model.Artist
import com.resofy.music.model.Home
import com.resofy.music.model.Song
import com.resofy.music.util.PreferenceUtil

class HomeAdapter(private val activity: AppCompatActivity) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), IArtistClickListener, IAlbumClickListener {

    private var list = listOf<Home>()

    override fun getItemViewType(position: Int): Int {
        return list[position].homeSection
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout =
            LayoutInflater.from(activity).inflate(R.layout.section_recycler_view, parent, false)
        return when (viewType) {
            RECENT_ARTISTS, TOP_ARTISTS -> ArtistViewHolder(layout)
            SUGGESTED_ARTISTS -> SuggestedArtistsViewHolder(layout)
            FAVOURITES -> PlaylistViewHolder(layout)
            HISTORY_PLAYLIST, LAST_ADDED_PLAYLIST -> RecentAlbumsViewHolder(layout)
            TOP_ALBUMS, RECENT_ALBUMS -> AlbumViewHolder(layout)
            else -> ArtistViewHolder(layout)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val home = list[position]
        when (getItemViewType(position)) {
            RECENT_ALBUMS -> {
                val viewHolder = holder as AlbumViewHolder
                viewHolder.bindView(home)
                viewHolder.clickableArea.setOnClickListener {
                    it.findFragment<HomeFragment>().setSharedAxisXTransitions()
                    activity.findNavController(R.id.fragment_container).navigate(
                        R.id.detailListFragment,
                        bundleOf("type" to RECENT_ALBUMS)
                    )
                }
            }
            TOP_ALBUMS -> {
                val viewHolder = holder as AlbumViewHolder
                viewHolder.bindView(home)
                viewHolder.clickableArea.setOnClickListener {
                    it.findFragment<HomeFragment>().setSharedAxisXTransitions()
                    activity.findNavController(R.id.fragment_container).navigate(
                        R.id.detailListFragment,
                        bundleOf("type" to TOP_ALBUMS)
                    )
                }
            }
            RECENT_ARTISTS -> {
                val viewHolder = holder as ArtistViewHolder
                viewHolder.bindView(home)
                viewHolder.clickableArea.setOnClickListener {
                    it.findFragment<HomeFragment>().setSharedAxisXTransitions()
                    activity.findNavController(R.id.fragment_container).navigate(
                        R.id.detailListFragment,
                        bundleOf("type" to RECENT_ARTISTS)
                    )
                }
            }
            TOP_ARTISTS -> {
                val viewHolder = holder as ArtistViewHolder
                viewHolder.bindView(home)
                viewHolder.clickableArea.setOnClickListener {
                    it.findFragment<HomeFragment>().setSharedAxisXTransitions()
                    activity.findNavController(R.id.fragment_container).navigate(
                        R.id.detailListFragment,
                        bundleOf("type" to TOP_ARTISTS)
                    )
                }
            }
            SUGGESTED_ARTISTS -> {
                val viewHolder = holder as SuggestedArtistsViewHolder
                viewHolder.bindView(home)
                // No clickableArea navigation — cada artista navega individualmente
            }
            FAVOURITES -> {
                val viewHolder = holder as PlaylistViewHolder
                viewHolder.bindView(home)
                viewHolder.clickableArea.setOnClickListener {
                    it.findFragment<HomeFragment>().setSharedAxisXTransitions()
                    activity.findNavController(R.id.fragment_container).navigate(
                        R.id.detailListFragment,
                        bundleOf("type" to FAVOURITES)
                    )
                }
            }
            HISTORY_PLAYLIST -> {
                val viewHolder = holder as RecentAlbumsViewHolder
                viewHolder.bindView(home)
            }
            LAST_ADDED_PLAYLIST -> {
                val viewHolder = holder as RecentAlbumsViewHolder
                viewHolder.bindView(home)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun swapData(sections: List<Home>) {
        list = sections
        notifyDataSetChanged()
    }

    @Suppress("UNCHECKED_CAST")
    private inner class AlbumViewHolder(view: View) : AbsHomeViewItem(view) {
        fun bindView(home: Home) {
            title.setText(home.titleRes)
            recyclerView.apply {
                adapter = AlbumAdapter(
                    activity,
                    home.arrayList as List<Album>,
                    R.layout.item_favourite_card,
                    this@HomeAdapter
                )
                layoutManager = gridLayoutManager()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inner class ArtistViewHolder(view: View) : AbsHomeViewItem(view) {
        fun bindView(home: Home) {
            title.setText(home.titleRes)
            recyclerView.apply {
                layoutManager = linearLayoutManager()
                adapter = artistsAdapter(home.arrayList as List<Artist>)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inner class SuggestedArtistsViewHolder(view: View) : AbsHomeViewItem(view) {
        fun bindView(home: Home) {
            title.setText(home.titleRes)
            recyclerView.apply {
                layoutManager = linearLayoutManager()
                adapter = ArtistAdapter(
                    activity,
                    home.arrayList as List<Artist>,
                    R.layout.item_artist,
                    this@HomeAdapter
                )
            }
        }
    }

    private inner class FavouriteSongAdapter(
        activity: AppCompatActivity,
        songs: MutableList<Song>,
    ) : SongAdapter(activity, songs, R.layout.item_favourite_card) {
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            super.onBindViewHolder(holder, position)
            holder.favoriteIcon?.isVisible = true
        }
    }
    @Suppress("UNCHECKED_CAST")
    private inner class PlaylistViewHolder(view: View) : AbsHomeViewItem(view) {
        fun bindView(home: Home) {
            title.setText(home.titleRes)
            recyclerView.apply {
                val songAdapter = FavouriteSongAdapter(
                    activity,
                    home.arrayList as MutableList<Song>
                )
                layoutManager = linearLayoutManager()
                adapter = songAdapter
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inner class RecentAlbumsViewHolder(view: View) : AbsHomeViewItem(view) {
        fun bindView(home: Home) {
            title.setText(home.titleRes)
            recyclerView.apply {
                adapter = AlbumAdapter(
                    activity,
                    home.arrayList as List<Album>,
                    R.layout.item_favourite_card,
                    this@HomeAdapter
                )
                layoutManager = gridLayoutManager()
            }
        }
    }
    open class AbsHomeViewItem(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)
        val title: AppCompatTextView = itemView.findViewById(R.id.title)
        val clickableArea: ViewGroup = itemView.findViewById(R.id.clickable_area)
    }

    private fun artistsAdapter(artists: List<Artist>) =
        ArtistAdapter(activity, artists, PreferenceUtil.homeArtistGridStyle, this)

    private fun albumAdapter(albums: List<Album>) =
        AlbumAdapter(activity, albums, PreferenceUtil.homeAlbumGridStyle, this)

    private fun gridLayoutManager() =
        GridLayoutManager(activity, 1, GridLayoutManager.HORIZONTAL, false)

    private fun linearLayoutManager() =
        LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

    override fun onArtist(artistId: Long, view: View) {
        activity.findNavController(R.id.fragment_container).navigate(
            R.id.artistDetailsFragment,
            bundleOf(EXTRA_ARTIST_ID to artistId),
            null,
            FragmentNavigatorExtras(
                view to artistId.toString()
            )
        )
    }

    override fun onAlbumClick(albumId: Long, view: View) {
        activity.findNavController(R.id.fragment_container).navigate(
            R.id.albumDetailsFragment,
            bundleOf(EXTRA_ALBUM_ID to albumId),
            null,
            FragmentNavigatorExtras(
                view to albumId.toString()
            )
        )
    }
}
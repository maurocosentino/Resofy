package com.resofy.music.interfaces

import com.resofy.music.model.Album
import com.resofy.music.model.Artist
import com.resofy.music.model.Genre

interface IHomeClickListener {
    fun onAlbumClick(album: Album)

    fun onArtistClick(artist: Artist)

    fun onGenreClick(genre: Genre)
}
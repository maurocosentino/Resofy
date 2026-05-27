package com.resofy.music.interfaces

import android.view.View
import com.resofy.music.model.Genre

interface IGenreClickListener {
    fun onClickGenre(genre: Genre, view: View)
}
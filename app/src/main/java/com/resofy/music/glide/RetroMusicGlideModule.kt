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
package com.resofy.music.glide

import android.content.Context
import android.graphics.Bitmap
import com.resofy.music.glide.artistimage.ArtistImage
import com.resofy.music.glide.artistimage.Factory
import com.resofy.music.glide.audiocover.AudioFileCover
import com.resofy.music.glide.audiocover.AudioFileCoverLoader
import com.resofy.music.glide.palette.BitmapPaletteTranscoder
import com.resofy.music.glide.palette.BitmapPaletteWrapper
import com.resofy.music.glide.playlistPreview.PlaylistPreview
import com.resofy.music.glide.playlistPreview.PlaylistPreviewLoader
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import java.io.InputStream

@GlideModule
class ResofyGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(
            PlaylistPreview::class.java,
            Bitmap::class.java,
            PlaylistPreviewLoader.Factory(context)
        )
        registry.prepend(
            AudioFileCover::class.java,
            InputStream::class.java,
            AudioFileCoverLoader.Factory()
        )
        registry.prepend(ArtistImage::class.java, InputStream::class.java, Factory(context))
        registry.register(
            Bitmap::class.java,
            BitmapPaletteWrapper::class.java, BitmapPaletteTranscoder()
        )
    }

    override fun isManifestParsingEnabled(): Boolean {
        return false
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDiskCache(
            com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory(
                context,
                "image_cache",
                250L * 1024 * 1024  // 250 MB
            )
        )
    }
}
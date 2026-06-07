package com.resofy.music.fragments.artists

import androidx.lifecycle.*
import com.resofy.music.interfaces.IMusicServiceEventListener
import com.resofy.music.model.Album
import com.resofy.music.model.Artist
import com.resofy.music.musicprovider.ProviderManager
import com.resofy.music.network.Result
import com.resofy.music.network.model.LastFmArtist
import com.resofy.music.repository.RealRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class ArtistDetailsViewModel(
    private val realRepository: RealRepository,
    private val providerManager: ProviderManager,
    private val artistId: Long?,
    private val artistName: String?
) : ViewModel(), IMusicServiceEventListener {

    private val artistDetails = MutableLiveData<Artist>()

    init {
        fetchArtist()
    }

    private fun fetchArtist() {
        viewModelScope.launch(IO) {
            if (artistId != null) {
                val artist = providerManager.activeProvider.artistById(artistId)
                    ?: realRepository.artistById(artistId)
                artistDetails.postValue(artist)
            } else if (artistName != null) {
                try {
                    val cached = providerManager.activeProvider.artistByName(artistName)
                    if (cached != null) {
                        artistDetails.postValue(cached)
                    } else {
                        artistDetails.postValue(realRepository.albumArtistByName(artistName))
                    }
                } catch (e: Exception) {
                    artistDetails.postValue(Artist.empty)
                }
            }
        }
    }

    fun refreshArtistInfo() {
        fetchArtist()
    }

    fun getArtist(): LiveData<Artist> = artistDetails

    fun getAlbumsForArtist(artistId: Long): LiveData<List<Album>> = liveData(IO) {
        val albums = providerManager.activeProvider.albumsForArtist(artistId)
        if (albums.isNotEmpty()) emit(albums)
    }

    fun getArtistInfo(
        name: String,
        lang: String?,
        cache: String?
    ): LiveData<Result<LastFmArtist>> = liveData(IO) {
        emit(Result.Loading)
        emit(realRepository.artistInfo(name, lang, cache))
    }

    override fun onMediaStoreChanged() { fetchArtist() }
    override fun onServiceConnected() {}
    override fun onServiceDisconnected() {}
    override fun onQueueChanged() {}
    override fun onPlayingMetaChanged() {}
    override fun onPlayStateChanged() {}
    override fun onRepeatModeChanged() {}
    override fun onShuffleModeChanged() {}
    override fun onFavoriteStateChanged() {}
}
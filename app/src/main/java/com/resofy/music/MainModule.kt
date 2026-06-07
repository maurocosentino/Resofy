package com.resofy.music

import androidx.room.Room
import com.resofy.music.auto.AutoMusicProvider
import com.resofy.music.cast.RetroWebServer
import com.resofy.music.db.MIGRATION_23_24
import com.resofy.music.db.MIGRATION_24_25
import com.resofy.music.db.RetroDatabase
import com.resofy.music.fragments.LibraryViewModel
import com.resofy.music.fragments.albums.AlbumDetailsViewModel
import com.resofy.music.fragments.artists.ArtistDetailsViewModel
import com.resofy.music.fragments.genres.GenreDetailsViewModel
import com.resofy.music.fragments.playlists.PlaylistDetailsViewModel
import com.resofy.music.fragments.settings.MusicProvidersViewModel
import com.resofy.music.model.Genre
import com.resofy.music.network.logInterceptor
import com.resofy.music.network.provideDefaultCache
import com.resofy.music.network.provideLastFmRest
import com.resofy.music.network.provideLastFmRetrofit
import com.resofy.music.network.provideOkHttp
import com.resofy.music.network.subsonic.SubsonicService
import com.resofy.music.repository.*
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.resofy.music.musicprovider.ProviderManager

val networkModule = module {

    factory {
        provideDefaultCache()
    }
    factory {
        provideOkHttp(get(), get())
    }
    single {
        provideLastFmRetrofit(get())
    }
    single {
        provideLastFmRest(get())
    }
}

private val roomModule = module {

    single {
        Room.databaseBuilder(androidContext(), RetroDatabase::class.java, "playlist.db")
            .addMigrations(MIGRATION_23_24, MIGRATION_24_25)
            .build()
    }

    factory {
        get<RetroDatabase>().serverConfigDao()
    }

    factory {
        get<RetroDatabase>().playlistDao()
    }

    factory {
        get<RetroDatabase>().playCountDao()
    }

    factory {
        get<RetroDatabase>().historyDao()
    }

    single {
        RealRoomRepository(get(), get(), get())
    } bind RoomRepository::class
}
private val autoModule = module {
    single {
        AutoMusicProvider(
            androidContext(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}
private val mainModule = module {
    single {
        androidContext().contentResolver
    }
    single {
        RetroWebServer(get())
    }
}
private val dataModule = module {
    single {
        RealRepository(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    } bind Repository::class

    single {
        RealSongRepository(get())
    } bind SongRepository::class

    single {
        RealGenreRepository(get(), get())
    } bind GenreRepository::class

    single {
        RealAlbumRepository(get())
    } bind AlbumRepository::class

    single {
        RealArtistRepository(get(), get())
    } bind ArtistRepository::class

    single {
        RealPlaylistRepository(get())
    } bind PlaylistRepository::class

    single {
        RealTopPlayedRepository(get(), get(), get(), get())
    } bind TopPlayedRepository::class

    single {
        RealLastAddedRepository(
            get(),
            get(),
            get()
        )
    } bind LastAddedRepository::class

    single {
        RealSearchRepository(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    single {
        RealLocalDataRepository(get())
    } bind LocalDataRepository::class

    single {
        ProviderManager(androidContext(), get(), get())
    }
    single {
        ServerConfigRepository(get())
    }
}

private val viewModules = module {

    viewModel {
        LibraryViewModel(get(), get())
    }

    viewModel { (albumId: Long) ->
        AlbumDetailsViewModel(get(), get(), albumId)
    }

    viewModel { (artistId: Long?, artistName: String?) ->
        ArtistDetailsViewModel(get(), get(), artistId, artistName)
    }

    viewModel { (playlistId: Long) ->
        PlaylistDetailsViewModel(
            get(),
            playlistId
        )
    }

    viewModel { (genre: Genre) ->
        GenreDetailsViewModel(
            get(),
            genre
        )
    }
    viewModel {
        MusicProvidersViewModel(get())
    }
}

val subsonicModule = module {
    factory { (baseUrl: String, username: String, password: String) ->
        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(logInterceptor())
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(baseUrl.trimEnd('/') + "/")
            .addConverterFactory(GsonConverterFactory.create())
            .callFactory { client.newCall(it) }
            .build()
            .create(SubsonicService::class.java)
    }
}

val appModules = listOf(mainModule, dataModule, autoModule, viewModules, networkModule, roomModule, subsonicModule)
package tv.trakt.trakt.app.core.sync.di

import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module
import tv.trakt.trakt.app.core.scrobble.data.local.ScrobbleUpdates
import tv.trakt.trakt.app.core.scrobble.data.local.ScrobbleUpdatesStorage
import tv.trakt.trakt.app.core.scrobble.data.work.PostScrobbleStartWorker
import tv.trakt.trakt.app.core.scrobble.data.work.PostScrobbleStopWorker
import tv.trakt.trakt.app.core.sync.data.local.episodes.EpisodesSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.local.episodes.EpisodesSyncStorage
import tv.trakt.trakt.app.core.sync.data.local.movies.MoviesSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.local.movies.MoviesSyncStorage
import tv.trakt.trakt.app.core.sync.data.local.shows.ShowsSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.local.shows.ShowsSyncStorage
import tv.trakt.trakt.app.core.sync.data.remote.episodes.EpisodesSyncApiClient
import tv.trakt.trakt.app.core.sync.data.remote.episodes.EpisodesSyncRemoteDataSource
import tv.trakt.trakt.app.core.sync.data.remote.movies.MoviesSyncApiClient
import tv.trakt.trakt.app.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.app.core.sync.data.remote.shows.ShowsSyncApiClient
import tv.trakt.trakt.app.core.sync.data.remote.shows.ShowsSyncRemoteDataSource

internal val syncModule = module {
    single<ShowsSyncRemoteDataSource> {
        ShowsSyncApiClient(
            usersApi = get(),
            syncApi = get(),
            watchedApi = get(),
            collectionApi = get(),
            cacheMarkerProvider = get(),
        )
    }

    single<MoviesSyncRemoteDataSource> {
        MoviesSyncApiClient(
            usersApi = get(),
            syncApi = get(),
            watchedApi = get(),
            collectionApi = get(),
            cacheMarkerProvider = get(),
        )
    }

    single<EpisodesSyncRemoteDataSource> {
        EpisodesSyncApiClient(
            syncApi = get(),
            scrobbleExtrasApi = get(),
            cacheMarkerProvider = get(),
        )
    }

    single<ShowsSyncLocalDataSource> {
        ShowsSyncStorage()
    }

    single<MoviesSyncLocalDataSource> {
        MoviesSyncStorage()
    }

    single<EpisodesSyncLocalDataSource> {
        EpisodesSyncStorage()
    }

    single<ScrobbleUpdates> {
        ScrobbleUpdatesStorage()
    }

    worker {
        PostScrobbleStartWorker(
            appContext = androidApplication(),
            workerParams = get(),
            sessionManager = get(),
            scrobbleExtrasApi = get(),
            scrobbleApi = get(),
            scrobbleUpdates = get(),
            cacheMarker = get(),
        )
    }

    worker {
        PostScrobbleStopWorker(
            appContext = androidApplication(),
            workerParams = get(),
            sessionManager = get(),
            scrobbleExtrasApi = get(),
            scrobbleApi = get(),
            scrobbleUpdates = get(),
            cacheMarker = get(),
        )
    }
}

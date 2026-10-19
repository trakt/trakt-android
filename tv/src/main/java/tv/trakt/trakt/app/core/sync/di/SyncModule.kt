package tv.trakt.trakt.app.core.sync.di

import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.app.core.scrobble.data.work.PostScrobbleStartWorker
import tv.trakt.trakt.app.core.scrobble.data.work.PostScrobbleStopWorker
import tv.trakt.trakt.app.core.sync.data.local.episodes.EpisodesSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.local.episodes.EpisodesSyncStorage
import tv.trakt.trakt.app.core.sync.data.local.movies.MoviesSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.local.movies.MoviesSyncStorage
import tv.trakt.trakt.app.core.sync.data.local.shows.ShowsSyncLocalDataSource
import tv.trakt.trakt.app.core.sync.data.local.shows.ShowsSyncStorage
import tv.trakt.trakt.common.core.user.CollectionStateProvider

internal val syncModule = module {
    single<ShowsSyncLocalDataSource> {
        ShowsSyncStorage()
    }

    single<MoviesSyncLocalDataSource> {
        MoviesSyncStorage()
    }

    single<EpisodesSyncLocalDataSource> {
        EpisodesSyncStorage()
    }

    singleOf(::CollectionStateProvider)

    worker {
        PostScrobbleStartWorker(
            appContext = androidApplication(),
            workerParams = get(),
            sessionManager = get(),
            scrobbleApi = get(),
            cacheMarker = get(),
        )
    }

    worker {
        PostScrobbleStopWorker(
            appContext = androidApplication(),
            workerParams = get(),
            sessionManager = get(),
            scrobbleApi = get(),
            dataChanges = get(),
            cacheMarker = get(),
        )
    }
}

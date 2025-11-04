package tv.trakt.trakt.core.sync.di

import org.koin.dsl.module
import tv.trakt.trakt.core.sync.data.remote.episodes.EpisodesSyncApiClient
import tv.trakt.trakt.core.sync.data.remote.episodes.EpisodesSyncRemoteDataSource
import tv.trakt.trakt.core.sync.data.remote.movies.MoviesSyncApiClient
import tv.trakt.trakt.core.sync.data.remote.movies.MoviesSyncRemoteDataSource
import tv.trakt.trakt.core.sync.data.remote.shows.ShowsSyncApiClient
import tv.trakt.trakt.core.sync.data.remote.shows.ShowsSyncRemoteDataSource
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieFavoritesUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieWatchlistUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowWatchlistUseCase

internal val syncModule = module {
    single<ShowsSyncRemoteDataSource> {
        ShowsSyncApiClient(
            syncApi = get(),
            usersApi = get(),
            cacheMarker = get(),
        )
    }

    single<MoviesSyncRemoteDataSource> {
        MoviesSyncApiClient(
            syncApi = get(),
            cacheMarker = get(),
        )
    }

    single<EpisodesSyncRemoteDataSource> {
        EpisodesSyncApiClient(
            syncApi = get(),
            cacheMarker = get(),
        )
    }

    factory {
        UpdateMovieHistoryUseCase(
            remoteSource = get(),
        )
    }

    factory {
        UpdateMovieWatchlistUseCase(
            remoteSource = get(),
        )
    }

    factory {
        UpdateMovieFavoritesUseCase(
            remoteSource = get(),
        )
    }

    factory {
        UpdateShowWatchlistUseCase(
            remoteSource = get(),
        )
    }

    factory {
        UpdateEpisodeHistoryUseCase(
            remoteSource = get(),
        )
    }

    factory {
        UpdateShowHistoryUseCase(
            remoteSource = get(),
        )
    }
}

package tv.trakt.trakt.core.sync.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.user.CollectionStateProvider
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
import tv.trakt.trakt.core.sync.usecases.UpdateShowFavoritesUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowWatchlistUseCase

internal val syncModule = module {
    singleOf(::ShowsSyncApiClient) { bind<ShowsSyncRemoteDataSource>() }

    singleOf(::MoviesSyncApiClient) { bind<MoviesSyncRemoteDataSource>() }

    singleOf(::EpisodesSyncApiClient) { bind<EpisodesSyncRemoteDataSource>() }

    singleOf(::CollectionStateProvider)

    factoryOf(::UpdateMovieHistoryUseCase)

    factoryOf(::UpdateMovieWatchlistUseCase)

    factoryOf(::UpdateMovieFavoritesUseCase)

    factoryOf(::UpdateShowWatchlistUseCase)

    factoryOf(::UpdateShowFavoritesUseCase)

    factoryOf(::UpdateEpisodeHistoryUseCase)

    factoryOf(::UpdateShowHistoryUseCase)
}

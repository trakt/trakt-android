package tv.trakt.trakt.app.core.movies.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.app.core.movies.MoviesViewModel
import tv.trakt.trakt.app.core.movies.data.remote.MoviesApiClient
import tv.trakt.trakt.app.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.app.core.movies.features.anticipated.MoviesAnticipatedViewAllViewModel
import tv.trakt.trakt.app.core.movies.features.popular.MoviesPopularViewAllViewModel
import tv.trakt.trakt.app.core.movies.features.releases.MoviesReleasesViewAllViewModel
import tv.trakt.trakt.app.core.movies.features.trending.MoviesTrendingViewAllViewModel
import tv.trakt.trakt.app.core.movies.usecase.GetAnticipatedMoviesUseCase
import tv.trakt.trakt.app.core.movies.usecase.GetMoviesReleasesUseCase
import tv.trakt.trakt.app.core.movies.usecase.GetPopularMoviesUseCase
import tv.trakt.trakt.app.core.movies.usecase.GetRecommendedMoviesUseCase
import tv.trakt.trakt.app.core.movies.usecase.GetTrendingMoviesUseCase
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieStorage

internal val moviesDataModule = module {
    single<MoviesRemoteDataSource> {
        MoviesApiClient(
            api = get(),
            recommendationsApi = get(),
            calendarsApi = get(),
        )
    }

    single<MovieLocalDataSource> {
        MovieStorage()
    }
}

internal val moviesModule = module {
    factory {
        GetTrendingMoviesUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        GetPopularMoviesUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        GetAnticipatedMoviesUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        GetRecommendedMoviesUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        GetMoviesReleasesUseCase(
            remoteSource = get(),
            localMovieSource = get(),
        )
    }

    viewModel {
        MoviesViewModel(
            getTrendingMoviesUseCase = get(),
            getPopularMoviesUseCase = get(),
            getAnticipatedMoviesUseCase = get(),
            getReleasesMoviesUseCase = get(),
            sessionManager = get(),
            appLifecycleProvider = get(),
            collectionStateProvider = get(),
        )
    }

    viewModel {
        MoviesReleasesViewAllViewModel(
            getItemsUseCase = get(),
        )
    }

    viewModel {
        MoviesTrendingViewAllViewModel(
            getItemsUseCase = get(),
            collectionStateProvider = get(),
        )
    }

    viewModel {
        MoviesPopularViewAllViewModel(
            getItemsUseCase = get(),
            collectionStateProvider = get(),
        )
    }

    viewModel {
        MoviesAnticipatedViewAllViewModel(
            getItemsUseCase = get(),
            collectionStateProvider = get(),
        )
    }
}

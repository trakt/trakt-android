package tv.trakt.trakt.app.core.movies.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.MoviesApi
import org.openapitools.client.apis.RecommendationsApi
import tv.trakt.trakt.app.Config.API_BASE_URL
import tv.trakt.trakt.app.core.movies.MoviesViewModel
import tv.trakt.trakt.app.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.app.core.movies.data.local.MovieStorage
import tv.trakt.trakt.app.core.movies.data.remote.MoviesApiClient
import tv.trakt.trakt.app.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.app.core.movies.usecase.GetAnticipatedMoviesUseCase
import tv.trakt.trakt.app.core.movies.usecase.GetHotMoviesUseCase
import tv.trakt.trakt.app.core.movies.usecase.GetPopularMoviesUseCase
import tv.trakt.trakt.app.core.movies.usecase.GetRecommendedMoviesUseCase
import tv.trakt.trakt.app.core.movies.usecase.GetTrendingMoviesUseCase

internal val moviesDataModule = module {
    single<MoviesRemoteDataSource> {
        MoviesApiClient(
            api = MoviesApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = get(),
                httpClientConfig = get(named("clientConfig")),
            ),
            recommendationsApi = RecommendationsApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = get(),
                httpClientConfig = get(named("authorizedClientConfig")),
            ),
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
        GetHotMoviesUseCase(
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

    viewModel {
        MoviesViewModel(
            getTrendingMoviesUseCase = get(),
            getPopularMoviesUseCase = get(),
            getAnticipatedMoviesUseCase = get(),
            getHotMoviesUseCase = get(),
            getRecommendedMoviesUseCase = get(),
            sessionManager = get(),
        )
    }
}

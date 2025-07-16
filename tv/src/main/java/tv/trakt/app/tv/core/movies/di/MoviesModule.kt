package tv.trakt.app.tv.core.movies.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.MoviesApi
import org.openapitools.client.apis.RecommendationsApi
import tv.trakt.app.tv.Config.API_BASE_URL
import tv.trakt.app.tv.core.movies.MoviesViewModel
import tv.trakt.app.tv.core.movies.data.local.MovieLocalDataSource
import tv.trakt.app.tv.core.movies.data.local.MovieStorage
import tv.trakt.app.tv.core.movies.data.remote.MoviesApiClient
import tv.trakt.app.tv.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.app.tv.core.movies.usecase.GetAnticipatedMoviesUseCase
import tv.trakt.app.tv.core.movies.usecase.GetHotMoviesUseCase
import tv.trakt.app.tv.core.movies.usecase.GetPopularMoviesUseCase
import tv.trakt.app.tv.core.movies.usecase.GetRecommendedMoviesUseCase
import tv.trakt.app.tv.core.movies.usecase.GetTrendingMoviesUseCase

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

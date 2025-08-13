package tv.trakt.trakt.core.movies.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.MoviesApi
import org.openapitools.client.apis.RecommendationsApi
import tv.trakt.trakt.common.Config.API_BASE_URL
import tv.trakt.trakt.core.movies.MoviesViewModel
import tv.trakt.trakt.core.movies.data.remote.MoviesApiClient
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.core.movies.sections.anticipated.GetAnticipatedMoviesUseCase
import tv.trakt.trakt.core.movies.sections.anticipated.MoviesAnticipatedViewModel
import tv.trakt.trakt.core.movies.sections.hot.MoviesHotViewModel
import tv.trakt.trakt.core.movies.sections.hot.usecase.GetHotMoviesUseCase
import tv.trakt.trakt.core.movies.sections.popular.MoviesPopularViewModel
import tv.trakt.trakt.core.movies.sections.popular.usecase.GetPopularMoviesUseCase
import tv.trakt.trakt.core.movies.sections.trending.MoviesTrendingViewModel
import tv.trakt.trakt.core.movies.sections.trending.usecase.GetTrendingMoviesUseCase

internal val moviesDataModule = module {

    single<MoviesRemoteDataSource> {
        MoviesApiClient(
            moviesApi = MoviesApi(
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
}

internal val moviesModule = module {
    factory {
        GetTrendingMoviesUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetHotMoviesUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetPopularMoviesUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetAnticipatedMoviesUseCase(
            remoteSource = get(),
        )
    }

    viewModel {
        MoviesViewModel(
            sessionManager = get(),
        )
    }

    viewModel {
        MoviesTrendingViewModel(
            getTrendingUseCase = get(),
        )
    }

    viewModel {
        MoviesHotViewModel(
            getHotUseCase = get(),
        )
    }

    viewModel {
        MoviesAnticipatedViewModel(
            getAnticipatedUseCase = get(),
        )
    }

    viewModel {
        MoviesPopularViewModel(
            getPopularUseCase = get(),
        )
    }
}

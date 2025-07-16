package tv.trakt.app.tv.core.shows.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.RecommendationsApi
import org.openapitools.client.apis.ShowsApi
import tv.trakt.app.tv.Config.API_BASE_URL
import tv.trakt.app.tv.core.shows.ShowsViewModel
import tv.trakt.app.tv.core.shows.data.local.ShowLocalDataSource
import tv.trakt.app.tv.core.shows.data.local.ShowStorage
import tv.trakt.app.tv.core.shows.data.remote.ShowsApiClient
import tv.trakt.app.tv.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.app.tv.core.shows.usecase.GetAnticipatedShowsUseCase
import tv.trakt.app.tv.core.shows.usecase.GetHotShowsUseCase
import tv.trakt.app.tv.core.shows.usecase.GetPopularShowsUseCase
import tv.trakt.app.tv.core.shows.usecase.GetRecommendedShowsUseCase
import tv.trakt.app.tv.core.shows.usecase.GetTrendingShowsUseCase

internal val showsDataModule = module {
    single<ShowsRemoteDataSource> {
        ShowsApiClient(
            api = ShowsApi(
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

    single<ShowLocalDataSource> {
        ShowStorage()
    }
}

internal val showsModule = module {
    factory {
        GetTrendingShowsUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        GetHotShowsUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        GetPopularShowsUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        GetAnticipatedShowsUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        GetRecommendedShowsUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    viewModel {
        ShowsViewModel(
            getTrendingShowsUseCase = get(),
            getPopularShowsUseCase = get(),
            getAnticipatedShowsUseCase = get(),
            getHotShowsUseCase = get(),
            getRecommendedShowsUseCase = get(),
            sessionManager = get(),
        )
    }
}

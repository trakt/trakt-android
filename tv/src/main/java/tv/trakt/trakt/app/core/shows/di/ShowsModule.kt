package tv.trakt.trakt.app.core.shows.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.RecommendationsApi
import org.openapitools.client.apis.ShowsApi
import tv.trakt.trakt.app.core.shows.ShowsViewModel
import tv.trakt.trakt.app.core.shows.data.remote.ShowsApiClient
import tv.trakt.trakt.app.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.app.core.shows.features.anticipated.ShowsAnticipatedViewAllViewModel
import tv.trakt.trakt.app.core.shows.features.popular.ShowsPopularViewAllViewModel
import tv.trakt.trakt.app.core.shows.features.recommended.ShowsRecommendedViewAllViewModel
import tv.trakt.trakt.app.core.shows.features.trending.ShowsTrendingViewAllViewModel
import tv.trakt.trakt.app.core.shows.usecase.GetAnticipatedShowsUseCase
import tv.trakt.trakt.app.core.shows.usecase.GetPopularShowsUseCase
import tv.trakt.trakt.app.core.shows.usecase.GetRecommendedShowsUseCase
import tv.trakt.trakt.app.core.shows.usecase.GetTrendingShowsUseCase
import tv.trakt.trakt.common.Config.API_BASE_URL
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowStorage

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
            getRecommendedShowsUseCase = get(),
            sessionManager = get(),
        )
    }

    viewModel {
        ShowsTrendingViewAllViewModel(
            getItemsUseCase = get(),
        )
    }

    viewModel {
        ShowsPopularViewAllViewModel(
            getItemsUseCase = get(),
        )
    }

    viewModel {
        ShowsAnticipatedViewAllViewModel(
            getItemsUseCase = get(),
        )
    }

    viewModel {
        ShowsRecommendedViewAllViewModel(
            getItemsUseCase = get(),
        )
    }
}

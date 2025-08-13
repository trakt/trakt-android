package tv.trakt.trakt.core.shows.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.ShowsApi
import tv.trakt.trakt.common.Config.API_BASE_URL
import tv.trakt.trakt.core.shows.ShowsViewModel
import tv.trakt.trakt.core.shows.data.remote.ShowsApiClient
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.shows.sections.anticipated.ShowsAnticipatedViewModel
import tv.trakt.trakt.core.shows.sections.anticipated.usecase.GetAnticipatedShowsUseCase
import tv.trakt.trakt.core.shows.sections.hot.ShowsHotViewModel
import tv.trakt.trakt.core.shows.sections.hot.usecase.GetHotShowsUseCase
import tv.trakt.trakt.core.shows.sections.popular.ShowsPopularViewModel
import tv.trakt.trakt.core.shows.sections.popular.usecase.GetPopularShowsUseCase
import tv.trakt.trakt.core.shows.sections.trending.ShowsTrendingViewModel
import tv.trakt.trakt.core.shows.sections.trending.usecase.GetTrendingShowsUseCase

internal val showsDataModule = module {

    single<ShowsRemoteDataSource> {
        ShowsApiClient(
            api = ShowsApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = get(),
                httpClientConfig = get(named("clientConfig")),
            ),
        )
    }
}

internal val showsModule = module {
    factory {
        GetTrendingShowsUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetHotShowsUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetPopularShowsUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetAnticipatedShowsUseCase(
            remoteSource = get(),
        )
    }

    viewModel {
        ShowsViewModel(
            sessionManager = get(),
        )
    }

    viewModel {
        ShowsTrendingViewModel(
            getTrendingUseCase = get(),
        )
    }

    viewModel {
        ShowsHotViewModel(
            getHotUseCase = get(),
        )
    }

    viewModel {
        ShowsAnticipatedViewModel(
            getAnticipatedUseCase = get(),
        )
    }

    viewModel {
        ShowsPopularViewModel(
            getPopularUseCase = get(),
        )
    }
}

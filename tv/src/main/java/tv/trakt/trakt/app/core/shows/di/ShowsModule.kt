package tv.trakt.trakt.app.core.shows.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
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
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowStorage

internal val showsDataModule = module {
    single<ShowsRemoteDataSource> {
        ShowsApiClient(
            api = get(),
            recommendationsApi = get(),
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
            appLifecycleProvider = get(),
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

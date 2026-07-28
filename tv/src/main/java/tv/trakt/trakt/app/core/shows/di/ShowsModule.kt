package tv.trakt.trakt.app.core.shows.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.app.core.shows.ShowsViewModel
import tv.trakt.trakt.app.core.shows.data.remote.ShowsApiClient
import tv.trakt.trakt.app.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.app.core.shows.features.anticipated.ShowsAnticipatedViewAllViewModel
import tv.trakt.trakt.app.core.shows.features.popular.ShowsPopularViewAllViewModel
import tv.trakt.trakt.app.core.shows.features.releases.ShowsReleasesViewAllViewModel
import tv.trakt.trakt.app.core.shows.features.trending.ShowsTrendingViewAllViewModel
import tv.trakt.trakt.app.core.shows.usecase.GetAnticipatedShowsUseCase
import tv.trakt.trakt.app.core.shows.usecase.GetPopularShowsUseCase
import tv.trakt.trakt.app.core.shows.usecase.GetRecommendedShowsUseCase
import tv.trakt.trakt.app.core.shows.usecase.GetShowsReleasesUseCase
import tv.trakt.trakt.app.core.shows.usecase.GetTrendingShowsUseCase
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowStorage

internal val showsDataModule = module {
    single<ShowsRemoteDataSource> {
        ShowsApiClient(
            api = get(),
            recommendationsApi = get(),
            calendarsApi = get(),
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

    factory {
        GetShowsReleasesUseCase(
            remoteSource = get(),
            localShowSource = get(),
            localEpisodeSource = get(),
        )
    }

    viewModel {
        ShowsViewModel(
            getTrendingShowsUseCase = get(),
            getPopularShowsUseCase = get(),
            getAnticipatedShowsUseCase = get(),
            getReleasesShowsUseCase = get(),
            sessionManager = get(),
            appLifecycleProvider = get(),
            collectionStateProvider = get(),
        )
    }

    viewModel {
        ShowsReleasesViewAllViewModel(
            getItemsUseCase = get(),
        )
    }

    viewModel {
        ShowsTrendingViewAllViewModel(
            getItemsUseCase = get(),
            collectionStateProvider = get(),
        )
    }

    viewModel {
        ShowsPopularViewAllViewModel(
            getItemsUseCase = get(),
            collectionStateProvider = get(),
        )
    }

    viewModel {
        ShowsAnticipatedViewAllViewModel(
            getItemsUseCase = get(),
            collectionStateProvider = get(),
        )
    }
}

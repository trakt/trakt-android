package tv.trakt.trakt.core.shows.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.core.shows.ShowsViewModel
import tv.trakt.trakt.core.shows.data.remote.ShowsApiClient
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.shows.sections.anticipated.ShowsAnticipatedViewModel
import tv.trakt.trakt.core.shows.sections.anticipated.data.local.AnticipatedShowsLocalDataSource
import tv.trakt.trakt.core.shows.sections.anticipated.data.local.AnticipatedShowsStorage
import tv.trakt.trakt.core.shows.sections.anticipated.usecase.GetAnticipatedShowsUseCase
import tv.trakt.trakt.core.shows.sections.popular.ShowsPopularViewModel
import tv.trakt.trakt.core.shows.sections.popular.data.local.PopularShowsLocalDataSource
import tv.trakt.trakt.core.shows.sections.popular.data.local.PopularShowsStorage
import tv.trakt.trakt.core.shows.sections.popular.usecase.GetPopularShowsUseCase
import tv.trakt.trakt.core.shows.sections.recommended.ShowsRecommendedViewModel
import tv.trakt.trakt.core.shows.sections.recommended.data.local.RecommendedShowsLocalDataSource
import tv.trakt.trakt.core.shows.sections.recommended.data.local.RecommendedShowsStorage
import tv.trakt.trakt.core.shows.sections.recommended.usecase.GetRecommendedShowsUseCase
import tv.trakt.trakt.core.shows.sections.trending.ShowsTrendingViewModel
import tv.trakt.trakt.core.shows.sections.trending.data.local.TrendingShowsLocalDataSource
import tv.trakt.trakt.core.shows.sections.trending.data.local.TrendingShowsStorage
import tv.trakt.trakt.core.shows.sections.trending.usecase.GetTrendingShowsUseCase

internal val showsDataModule = module {
    single<ShowsRemoteDataSource> {
        ShowsApiClient(
            showsApi = get(),
            recommendationsApi = get(),
        )
    }

    single<TrendingShowsLocalDataSource> {
        TrendingShowsStorage()
    }

    single<RecommendedShowsLocalDataSource> {
        RecommendedShowsStorage()
    }

    single<PopularShowsLocalDataSource> {
        PopularShowsStorage()
    }

    single<AnticipatedShowsLocalDataSource> {
        AnticipatedShowsStorage()
    }
}

internal val showsModule = module {
    factory {
        GetTrendingShowsUseCase(
            remoteSource = get(),
            localTrendingSource = get(),
        )
    }

    factory {
        GetPopularShowsUseCase(
            remoteSource = get(),
            localPopularSource = get(),
        )
    }

    factory {
        GetAnticipatedShowsUseCase(
            remoteSource = get(),
            localAnticipatedSource = get(),
        )
    }

    factory {
        GetRecommendedShowsUseCase(
            remoteSource = get(),
            localRecommendedSource = get(),
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
        ShowsAnticipatedViewModel(
            getAnticipatedUseCase = get(),
        )
    }

    viewModel {
        ShowsRecommendedViewModel(
            getRecommendedUseCase = get(),
        )
    }

    viewModel {
        ShowsPopularViewModel(
            getPopularUseCase = get(),
        )
    }
}

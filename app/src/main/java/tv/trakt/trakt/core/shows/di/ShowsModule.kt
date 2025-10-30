package tv.trakt.trakt.core.shows.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.episodes.data.remote.EpisodesApiClient
import tv.trakt.trakt.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.core.shows.ShowsViewModel
import tv.trakt.trakt.core.shows.data.remote.ShowsApiClient
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
import tv.trakt.trakt.core.shows.sections.anticipated.ShowsAnticipatedViewModel
import tv.trakt.trakt.core.shows.sections.anticipated.all.AllShowsAnticipatedViewModel
import tv.trakt.trakt.core.shows.sections.anticipated.data.local.AnticipatedShowsLocalDataSource
import tv.trakt.trakt.core.shows.sections.anticipated.data.local.AnticipatedShowsStorage
import tv.trakt.trakt.core.shows.sections.anticipated.usecases.GetAnticipatedShowsUseCase
import tv.trakt.trakt.core.shows.sections.anticipated.usecases.anticipated.DefaultGetAnticipatedShowsUseCase
import tv.trakt.trakt.core.shows.sections.anticipated.usecases.anticipated.HalloweenGetAnticipatedShowsUseCase
import tv.trakt.trakt.core.shows.sections.popular.ShowsPopularViewModel
import tv.trakt.trakt.core.shows.sections.popular.all.AllShowsPopularViewModel
import tv.trakt.trakt.core.shows.sections.popular.data.local.PopularShowsLocalDataSource
import tv.trakt.trakt.core.shows.sections.popular.data.local.PopularShowsStorage
import tv.trakt.trakt.core.shows.sections.popular.usecase.GetPopularShowsUseCase
import tv.trakt.trakt.core.shows.sections.popular.usecase.popular.DefaultGetPopularShowsUseCase
import tv.trakt.trakt.core.shows.sections.popular.usecase.popular.HalloweenGetPopularShowsUseCase
import tv.trakt.trakt.core.shows.sections.recommended.ShowsRecommendedViewModel
import tv.trakt.trakt.core.shows.sections.recommended.all.AllShowsRecommendedViewModel
import tv.trakt.trakt.core.shows.sections.recommended.data.local.RecommendedShowsLocalDataSource
import tv.trakt.trakt.core.shows.sections.recommended.data.local.RecommendedShowsStorage
import tv.trakt.trakt.core.shows.sections.recommended.usecase.GetRecommendedShowsUseCase
import tv.trakt.trakt.core.shows.sections.recommended.usecase.recommended.DefaultGetRecommendedShowsUseCase
import tv.trakt.trakt.core.shows.sections.recommended.usecase.recommended.HalloweenGetRecommendedShowsUseCase
import tv.trakt.trakt.core.shows.sections.trending.ShowsTrendingViewModel
import tv.trakt.trakt.core.shows.sections.trending.all.AllShowsTrendingViewModel
import tv.trakt.trakt.core.shows.sections.trending.data.local.TrendingShowsLocalDataSource
import tv.trakt.trakt.core.shows.sections.trending.data.local.TrendingShowsStorage
import tv.trakt.trakt.core.shows.sections.trending.usecases.GetTrendingShowsUseCase
import tv.trakt.trakt.core.shows.sections.trending.usecases.trending.DefaultGetTrendingShowsUseCase
import tv.trakt.trakt.core.shows.sections.trending.usecases.trending.HalloweenGetTrendingShowsUseCase
import tv.trakt.trakt.core.shows.ui.context.ShowContextViewModel

internal val showsDataModule = module {
    single<ShowsRemoteDataSource> {
        ShowsApiClient(
            showsApi = get(),
            recommendationsApi = get(),
        )
    }

    single<EpisodesRemoteDataSource> {
        EpisodesApiClient(
            showsApi = get(),
            episodesApi = get(),
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
    factory<GetTrendingShowsUseCase>(
        qualifier = named("defaultTrendingShowsUseCase"),
    ) {
        DefaultGetTrendingShowsUseCase(
            remoteSource = get(),
            localTrendingSource = get(),
            localShowSource = get(),
        )
    }

    factory<GetTrendingShowsUseCase>(
        qualifier = named("halloweenTrendingShowsUseCase"),
    ) {
        HalloweenGetTrendingShowsUseCase(
            remoteSource = get(),
            localTrendingSource = get(),
            localShowSource = get(),
        )
    }

    factory<GetPopularShowsUseCase>(
        qualifier = named("defaultPopularShowsUseCase"),
    ) {
        DefaultGetPopularShowsUseCase(
            remoteSource = get(),
            localPopularSource = get(),
            localShowSource = get(),
        )
    }

    factory<GetPopularShowsUseCase>(
        qualifier = named("halloweenPopularShowsUseCase"),
    ) {
        HalloweenGetPopularShowsUseCase(
            remoteSource = get(),
            localPopularSource = get(),
            localShowSource = get(),
        )
    }

    factory<GetAnticipatedShowsUseCase>(
        qualifier = named("defaultAnticipatedShowsUseCase"),
    ) {
        DefaultGetAnticipatedShowsUseCase(
            remoteSource = get(),
            localAnticipatedSource = get(),
            localShowSource = get(),
        )
    }

    factory<GetAnticipatedShowsUseCase>(
        qualifier = named("halloweenAnticipatedShowsUseCase"),
    ) {
        HalloweenGetAnticipatedShowsUseCase(
            remoteSource = get(),
            localAnticipatedSource = get(),
            localShowSource = get(),
        )
    }

    factory<GetRecommendedShowsUseCase>(
        qualifier = named("defaultRecommendedShowsUseCase"),
    ) {
        DefaultGetRecommendedShowsUseCase(
            remoteSource = get(),
            localRecommendedSource = get(),
            localShowSource = get(),
        )
    }

    factory<GetRecommendedShowsUseCase>(
        qualifier = named("halloweenRecommendedShowsUseCase"),
    ) {
        HalloweenGetRecommendedShowsUseCase(
            remoteSource = get(),
            localRecommendedSource = get(),
            localShowSource = get(),
        )
    }

    viewModel {
        ShowsViewModel(
            sessionManager = get(),
            analytics = get(),
        )
    }

    viewModel { (halloween: Boolean) ->
        ShowsTrendingViewModel(
            getTrendingUseCase = when {
                halloween -> get(named("halloweenTrendingShowsUseCase"))
                else -> get(named("defaultTrendingShowsUseCase"))
            },
        )
    }

    viewModel { (halloween: Boolean) ->
        ShowsAnticipatedViewModel(
            getAnticipatedUseCase = when {
                halloween -> get(named("halloweenAnticipatedShowsUseCase"))
                else -> get(named("defaultAnticipatedShowsUseCase"))
            },
        )
    }

    viewModel { (halloween: Boolean) ->
        ShowsRecommendedViewModel(
            getRecommendedUseCase = when {
                halloween -> get(named("halloweenRecommendedShowsUseCase"))
                else -> get(named("defaultRecommendedShowsUseCase"))
            },
        )
    }

    viewModel { (halloween: Boolean) ->
        ShowsPopularViewModel(
            getPopularUseCase = when {
                halloween -> get(named("halloweenPopularShowsUseCase"))
                else -> get(named("defaultPopularShowsUseCase"))
            },
        )
    }

    viewModel { (halloween: Boolean) ->
        AllShowsTrendingViewModel(
            getTrendingUseCase = when {
                halloween -> get(named("halloweenTrendingShowsUseCase"))
                else -> get(named("defaultTrendingShowsUseCase"))
            },
            analytics = get(),
        )
    }

    viewModel { (halloween: Boolean) ->
        AllShowsPopularViewModel(
            getPopularUseCase = when {
                halloween -> get(named("halloweenPopularShowsUseCase"))
                else -> get(named("defaultPopularShowsUseCase"))
            },
            analytics = get(),
        )
    }

    viewModel { (halloween: Boolean) ->
        AllShowsAnticipatedViewModel(
            getAnticipatedUseCase = when {
                halloween -> get(named("halloweenAnticipatedShowsUseCase"))
                else -> get(named("defaultAnticipatedShowsUseCase"))
            },
            analytics = get(),
        )
    }

    viewModel { (halloween: Boolean) ->
        AllShowsRecommendedViewModel(
            getRecommendedUseCase = when {
                halloween -> get(named("halloweenRecommendedShowsUseCase"))
                else -> get(named("defaultRecommendedShowsUseCase"))
            },
            analytics = get(),
        )
    }

    viewModel { (show: Show) ->
        ShowContextViewModel(
            show = show,
            updateWatchlistUseCase = get(),
            updateHistoryUseCase = get(),
            userProgressLocalSource = get(),
            userWatchlistLocalSource = get(),
            loadProgressUseCase = get(),
            loadWatchlistUseCase = get(),
            sessionManager = get(),
        )
    }
}

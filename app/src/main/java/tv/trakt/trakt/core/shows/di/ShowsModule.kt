package tv.trakt.trakt.core.shows.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.discover.sections.anticipated.all.AllShowsAnticipatedViewModel
import tv.trakt.trakt.core.discover.sections.anticipated.data.local.shows.AnticipatedShowsLocalDataSource
import tv.trakt.trakt.core.discover.sections.anticipated.data.local.shows.AnticipatedShowsStorage
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.GetAnticipatedShowsUseCase
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.shows.DefaultGetAnticipatedShowsUseCase
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.shows.HalloweenGetAnticipatedShowsUseCase
import tv.trakt.trakt.core.discover.sections.popular.all.AllShowsPopularViewModel
import tv.trakt.trakt.core.discover.sections.popular.data.local.shows.PopularShowsLocalDataSource
import tv.trakt.trakt.core.discover.sections.popular.data.local.shows.PopularShowsStorage
import tv.trakt.trakt.core.discover.sections.popular.usecases.GetPopularShowsUseCase
import tv.trakt.trakt.core.discover.sections.popular.usecases.shows.DefaultGetPopularShowsUseCase
import tv.trakt.trakt.core.discover.sections.popular.usecases.shows.HalloweenGetPopularShowsUseCase
import tv.trakt.trakt.core.discover.sections.recommended.all.AllShowsRecommendedViewModel
import tv.trakt.trakt.core.discover.sections.recommended.data.local.shows.RecommendedShowsLocalDataSource
import tv.trakt.trakt.core.discover.sections.recommended.data.local.shows.RecommendedShowsStorage
import tv.trakt.trakt.core.discover.sections.recommended.usecase.GetRecommendedShowsUseCase
import tv.trakt.trakt.core.discover.sections.recommended.usecase.shows.DefaultGetRecommendedShowsUseCase
import tv.trakt.trakt.core.discover.sections.recommended.usecase.shows.HalloweenGetRecommendedShowsUseCase
import tv.trakt.trakt.core.discover.sections.trending.all.AllShowsTrendingViewModel
import tv.trakt.trakt.core.discover.sections.trending.data.local.shows.TrendingShowsLocalDataSource
import tv.trakt.trakt.core.discover.sections.trending.data.local.shows.TrendingShowsStorage
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingShowsUseCase
import tv.trakt.trakt.core.discover.sections.trending.usecases.shows.DefaultGetTrendingShowsUseCase
import tv.trakt.trakt.core.discover.sections.trending.usecases.shows.HalloweenGetTrendingShowsUseCase
import tv.trakt.trakt.core.episodes.data.remote.EpisodesApiClient
import tv.trakt.trakt.core.episodes.data.remote.EpisodesRemoteDataSource
import tv.trakt.trakt.core.shows.data.remote.ShowsApiClient
import tv.trakt.trakt.core.shows.data.remote.ShowsRemoteDataSource
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
            analytics = get(),
        )
    }
}

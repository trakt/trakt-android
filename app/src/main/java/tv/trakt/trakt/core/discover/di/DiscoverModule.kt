package tv.trakt.trakt.core.discover.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.core.discover.DiscoverViewModel
import tv.trakt.trakt.core.discover.sections.anticipated.DiscoverAnticipatedViewModel
import tv.trakt.trakt.core.discover.sections.popular.DiscoverPopularViewModel
import tv.trakt.trakt.core.discover.sections.recommended.DiscoverRecommendedViewModel
import tv.trakt.trakt.core.discover.sections.trending.DiscoverTrendingViewModel
import tv.trakt.trakt.core.discover.sections.trending.all.AllDiscoverTrendingViewModel

internal val discoverModule = module {
    viewModel {
        DiscoverViewModel(
            sessionManager = get(),
            analytics = get(),
        )
    }

    viewModel { (halloween: Boolean) ->
        DiscoverTrendingViewModel(
            modeProvider = get(),
            getTrendingShowsUseCase = when {
                halloween -> get(named("halloweenTrendingShowsUseCase"))
                else -> get(named("defaultTrendingShowsUseCase"))
            },
            getTrendingMoviesUseCase = when {
                halloween -> get(named("halloweenTrendingMoviesUseCase"))
                else -> get(named("defaultTrendingMoviesUseCase"))
            },
        )
    }

    viewModel { (halloween: Boolean) ->
        AllDiscoverTrendingViewModel(
            modeProvider = get(),
            getTrendingShowsUseCase = when {
                halloween -> get(named("halloweenTrendingShowsUseCase"))
                else -> get(named("defaultTrendingShowsUseCase"))
            },
            getTrendingMoviesUseCase = when {
                halloween -> get(named("halloweenTrendingMoviesUseCase"))
                else -> get(named("defaultTrendingMoviesUseCase"))
            },
            analytics = get(),
        )
    }

    viewModel { (halloween: Boolean) ->
        DiscoverAnticipatedViewModel(
            modeProvider = get(),
            getAnticipatedShowsUseCase = when {
                halloween -> get(named("halloweenAnticipatedShowsUseCase"))
                else -> get(named("defaultAnticipatedShowsUseCase"))
            },
            getAnticipatedMoviesUseCase = when {
                halloween -> get(named("halloweenAnticipatedMoviesUseCase"))
                else -> get(named("defaultAnticipatedMoviesUseCase"))
            },
        )
    }

    viewModel { (halloween: Boolean) ->
        DiscoverRecommendedViewModel(
            modeProvider = get(),
            getRecommendedShowsUseCase = when {
                halloween -> get(named("halloweenRecommendedShowsUseCase"))
                else -> get(named("defaultRecommendedShowsUseCase"))
            },
            getRecommendedMoviesUseCase = when {
                halloween -> get(named("halloweenRecommendedMoviesUseCase"))
                else -> get(named("defaultRecommendedMoviesUseCase"))
            },
        )
    }

    viewModel { (halloween: Boolean) ->
        DiscoverPopularViewModel(
            modeProvider = get(),
            getPopularShowsUseCase = when {
                halloween -> get(named("halloweenPopularShowsUseCase"))
                else -> get(named("defaultPopularShowsUseCase"))
            },
            getPopularMoviesUseCase = when {
                halloween -> get(named("halloweenPopularMoviesUseCase"))
                else -> get(named("defaultPopularMoviesUseCase"))
            },
        )
    }
}

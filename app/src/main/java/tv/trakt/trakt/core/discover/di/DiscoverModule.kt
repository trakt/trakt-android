package tv.trakt.trakt.core.discover.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.core.discover.DiscoverViewModel
import tv.trakt.trakt.core.discover.sections.all.AllDiscoverViewModel
import tv.trakt.trakt.core.discover.sections.all.usecases.GetAllDiscoverMoviesUseCase
import tv.trakt.trakt.core.discover.sections.all.usecases.GetAllDiscoverShowsUseCase
import tv.trakt.trakt.core.discover.sections.anticipated.DiscoverAnticipatedViewModel
import tv.trakt.trakt.core.discover.sections.popular.DiscoverPopularViewModel
import tv.trakt.trakt.core.discover.sections.recommended.DiscoverRecommendedViewModel
import tv.trakt.trakt.core.discover.sections.trending.DiscoverTrendingViewModel

internal val discoverModule = module {

    factory(
        qualifier = named("defaultAllDiscoverShowsUseCase"),
    ) {
        GetAllDiscoverShowsUseCase(
            getTrendingShowsUseCase = get(named("defaultTrendingShowsUseCase")),
            getAnticipatedShowsUseCase = get(named("defaultAnticipatedShowsUseCase")),
            getPopularShowsUseCase = get(named("defaultPopularShowsUseCase")),
            getRecommendedShowsUseCase = get(named("defaultRecommendedShowsUseCase")),
        )
    }

    factory(
        qualifier = named("defaultAllDiscoverMoviesUseCase"),
    ) {
        GetAllDiscoverMoviesUseCase(
            getTrendingMoviesUseCase = get(named("defaultTrendingMoviesUseCase")),
            getAnticipatedMoviesUseCase = get(named("defaultAnticipatedMoviesUseCase")),
            getPopularMoviesUseCase = get(named("defaultPopularMoviesUseCase")),
            getRecommendedMoviesUseCase = get(named("defaultRecommendedMoviesUseCase")),
        )
    }

    factory(
        qualifier = named("customAllDiscoverShowsUseCase"),
    ) {
        GetAllDiscoverShowsUseCase(
            getTrendingShowsUseCase = get(named("customTrendingShowsUseCase")),
            getAnticipatedShowsUseCase = get(named("customAnticipatedShowsUseCase")),
            getPopularShowsUseCase = get(named("customPopularShowsUseCase")),
            getRecommendedShowsUseCase = get(named("customRecommendedShowsUseCase")),
        )
    }

    factory(
        qualifier = named("customAllDiscoverMoviesUseCase"),
    ) {
        GetAllDiscoverMoviesUseCase(
            getTrendingMoviesUseCase = get(named("customTrendingMoviesUseCase")),
            getAnticipatedMoviesUseCase = get(named("customAnticipatedMoviesUseCase")),
            getPopularMoviesUseCase = get(named("customPopularMoviesUseCase")),
            getRecommendedMoviesUseCase = get(named("customRecommendedMoviesUseCase")),
        )
    }

    viewModel {
        DiscoverViewModel(
            sessionManager = get(),
            modeManager = get(),
            analytics = get(),
            collectionStateProvider = get(),
        )
    }

    viewModel { (customTheme: Boolean) ->
        AllDiscoverViewModel(
            savedStateHandle = get(),
            analytics = get(),
            modeManager = get(),
            getShowsUseCase = when {
                customTheme -> get(named("customAllDiscoverShowsUseCase"))
                else -> get(named("defaultAllDiscoverShowsUseCase"))
            },
            getMoviesUseCase = when {
                customTheme -> get(named("customAllDiscoverMoviesUseCase"))
                else -> get(named("defaultAllDiscoverMoviesUseCase"))
            },
            collectionStateProvider = get(),
        )
    }

    viewModel { (customTheme: Boolean) ->
        DiscoverTrendingViewModel(
            modeManager = get(),
            collapsingManager = get(),
            getTrendingShowsUseCase = when {
                customTheme -> get(named("customTrendingShowsUseCase"))
                else -> get(named("defaultTrendingShowsUseCase"))
            },
            getTrendingMoviesUseCase = when {
                customTheme -> get(named("customTrendingMoviesUseCase"))
                else -> get(named("defaultTrendingMoviesUseCase"))
            },
        )
    }

    viewModel { (customTheme: Boolean) ->
        DiscoverAnticipatedViewModel(
            modeManager = get(),
            getAnticipatedShowsUseCase = when {
                customTheme -> get(named("customAnticipatedShowsUseCase"))
                else -> get(named("defaultAnticipatedShowsUseCase"))
            },
            getAnticipatedMoviesUseCase = when {
                customTheme -> get(named("customAnticipatedMoviesUseCase"))
                else -> get(named("defaultAnticipatedMoviesUseCase"))
            },
        )
    }

    viewModel { (customTheme: Boolean) ->
        DiscoverPopularViewModel(
            modeManager = get(),
            getPopularShowsUseCase = when {
                customTheme -> get(named("customPopularShowsUseCase"))
                else -> get(named("defaultPopularShowsUseCase"))
            },
            getPopularMoviesUseCase = when {
                customTheme -> get(named("customPopularMoviesUseCase"))
                else -> get(named("defaultPopularMoviesUseCase"))
            },
        )
    }

    viewModel { (customTheme: Boolean) ->
        DiscoverRecommendedViewModel(
            modeManager = get(),
            getRecommendedShowsUseCase = when {
                customTheme -> get(named("customRecommendedShowsUseCase"))
                else -> get(named("defaultRecommendedShowsUseCase"))
            },
            getRecommendedMoviesUseCase = when {
                customTheme -> get(named("customRecommendedMoviesUseCase"))
                else -> get(named("defaultRecommendedMoviesUseCase"))
            },
        )
    }
}

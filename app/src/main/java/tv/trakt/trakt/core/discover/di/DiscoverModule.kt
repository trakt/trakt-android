package tv.trakt.trakt.core.discover.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.core.discover.DiscoverViewModel
import tv.trakt.trakt.core.discover.sections.all.AllDiscoverViewModel
import tv.trakt.trakt.core.discover.sections.anticipated.DiscoverAnticipatedViewModel
import tv.trakt.trakt.core.discover.sections.popular.DiscoverPopularViewModel
import tv.trakt.trakt.core.discover.sections.recommended.DiscoverRecommendedViewModel
import tv.trakt.trakt.core.discover.sections.trending.DiscoverTrendingViewModel

internal val discoverModule = module {
    viewModel {
        DiscoverViewModel(
            sessionManager = get(),
            analytics = get(),
        )
    }

    viewModel {
        DiscoverTrendingViewModel(
            modeProvider = get(),
            getTrendingShowsUseCase = get(named("defaultTrendingShowsUseCase")),
            getTrendingMoviesUseCase = get(named("defaultTrendingMoviesUseCase")),
        )
    }

    viewModel {
        AllDiscoverViewModel(
            savedStateHandle = get(),
            analytics = get(),
            modeProvider = get(),
            getTrendingShowsUseCase = get(named("defaultTrendingShowsUseCase")),
            getTrendingMoviesUseCase = get(named("defaultTrendingMoviesUseCase")),
            getPopularShowsUseCase = get(named("defaultPopularShowsUseCase")),
            getPopularMoviesUseCase = get(named("defaultPopularMoviesUseCase")),
            getAnticipatedShowsUseCase = get(named("defaultAnticipatedShowsUseCase")),
            getAnticipatedMoviesUseCase = get(named("defaultAnticipatedMoviesUseCase")),
            getRecommendedShowsUseCase = get(named("defaultRecommendedShowsUseCase")),
            getRecommendedMoviesUseCase = get(named("defaultRecommendedMoviesUseCase")),
        )
    }

    viewModel {
        DiscoverAnticipatedViewModel(
            modeProvider = get(),
            getAnticipatedShowsUseCase = get(named("defaultAnticipatedShowsUseCase")),
            getAnticipatedMoviesUseCase = get(named("defaultAnticipatedMoviesUseCase")),
        )
    }

    viewModel {
        DiscoverRecommendedViewModel(
            modeProvider = get(),
            getRecommendedShowsUseCase = get(named("defaultRecommendedShowsUseCase")),
            getRecommendedMoviesUseCase = get(named("defaultRecommendedMoviesUseCase")),
        )
    }

    viewModel {
        DiscoverPopularViewModel(
            modeProvider = get(),
            getPopularShowsUseCase = get(named("defaultPopularShowsUseCase")),
            getPopularMoviesUseCase = get(named("defaultPopularMoviesUseCase")),
        )
    }
}

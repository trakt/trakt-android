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

    factory {
        GetAllDiscoverShowsUseCase(
            getTrendingShowsUseCase = get(named("defaultTrendingShowsUseCase")),
            getAnticipatedShowsUseCase = get(named("defaultAnticipatedShowsUseCase")),
            getPopularShowsUseCase = get(named("defaultPopularShowsUseCase")),
            getRecommendedShowsUseCase = get(named("defaultRecommendedShowsUseCase")),
        )
    }

    factory {
        GetAllDiscoverMoviesUseCase(
            getTrendingMoviesUseCase = get(named("defaultTrendingMoviesUseCase")),
            getAnticipatedMoviesUseCase = get(named("defaultAnticipatedMoviesUseCase")),
            getPopularMoviesUseCase = get(named("defaultPopularMoviesUseCase")),
            getRecommendedMoviesUseCase = get(named("defaultRecommendedMoviesUseCase")),
        )
    }

    viewModel {
        DiscoverViewModel(
            sessionManager = get(),
            analytics = get(),
        )
    }

    viewModel {
        AllDiscoverViewModel(
            savedStateHandle = get(),
            analytics = get(),
            modeProvider = get(),
            getShowsUseCase = get(),
            getMoviesUseCase = get(),
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
        DiscoverAnticipatedViewModel(
            modeProvider = get(),
            getAnticipatedShowsUseCase = get(named("defaultAnticipatedShowsUseCase")),
            getAnticipatedMoviesUseCase = get(named("defaultAnticipatedMoviesUseCase")),
        )
    }

    viewModel {
        DiscoverPopularViewModel(
            modeProvider = get(),
            getPopularShowsUseCase = get(named("defaultPopularShowsUseCase")),
            getPopularMoviesUseCase = get(named("defaultPopularMoviesUseCase")),
        )
    }

    viewModel {
        DiscoverRecommendedViewModel(
            modeProvider = get(),
            getRecommendedShowsUseCase = get(named("defaultRecommendedShowsUseCase")),
            getRecommendedMoviesUseCase = get(named("defaultRecommendedMoviesUseCase")),
        )
    }
}

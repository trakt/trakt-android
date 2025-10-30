package tv.trakt.trakt.core.movies.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.movies.MoviesViewModel
import tv.trakt.trakt.core.movies.data.remote.MoviesApiClient
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
import tv.trakt.trakt.core.movies.sections.anticipated.MoviesAnticipatedViewModel
import tv.trakt.trakt.core.movies.sections.anticipated.all.AllMoviesAnticipatedViewModel
import tv.trakt.trakt.core.movies.sections.anticipated.data.local.AnticipatedMoviesLocalDataSource
import tv.trakt.trakt.core.movies.sections.anticipated.data.local.AnticipatedMoviesStorage
import tv.trakt.trakt.core.movies.sections.anticipated.usecase.GetAnticipatedMoviesUseCase
import tv.trakt.trakt.core.movies.sections.anticipated.usecase.anticipated.DefaultGetAnticipatedMoviesUseCase
import tv.trakt.trakt.core.movies.sections.anticipated.usecase.anticipated.HalloweenGetAnticipatedMoviesUseCase
import tv.trakt.trakt.core.movies.sections.popular.MoviesPopularViewModel
import tv.trakt.trakt.core.movies.sections.popular.all.AllMoviesPopularViewModel
import tv.trakt.trakt.core.movies.sections.popular.data.local.PopularMoviesLocalDataSource
import tv.trakt.trakt.core.movies.sections.popular.data.local.PopularMoviesStorage
import tv.trakt.trakt.core.movies.sections.popular.usecase.GetPopularMoviesUseCase
import tv.trakt.trakt.core.movies.sections.popular.usecase.popular.DefaultGetPopularMoviesUseCase
import tv.trakt.trakt.core.movies.sections.popular.usecase.popular.HalloweenGetPopularMoviesUseCase
import tv.trakt.trakt.core.movies.sections.recommended.MoviesRecommendedViewModel
import tv.trakt.trakt.core.movies.sections.recommended.all.AllMoviesRecommendedViewModel
import tv.trakt.trakt.core.movies.sections.recommended.data.local.RecommendedMoviesLocalDataSource
import tv.trakt.trakt.core.movies.sections.recommended.data.local.RecommendedMoviesStorage
import tv.trakt.trakt.core.movies.sections.recommended.usecase.GetRecommendedMoviesUseCase
import tv.trakt.trakt.core.movies.sections.recommended.usecase.recommended.DefaultGetRecommendedMoviesUseCase
import tv.trakt.trakt.core.movies.sections.recommended.usecase.recommended.HalloweenGetRecommendedMoviesUseCase
import tv.trakt.trakt.core.movies.sections.trending.MoviesTrendingViewModel
import tv.trakt.trakt.core.movies.sections.trending.all.AllMoviesTrendingViewModel
import tv.trakt.trakt.core.movies.sections.trending.data.local.TrendingMoviesLocalDataSource
import tv.trakt.trakt.core.movies.sections.trending.data.local.TrendingMoviesStorage
import tv.trakt.trakt.core.movies.sections.trending.usecase.GetTrendingMoviesUseCase
import tv.trakt.trakt.core.movies.sections.trending.usecase.trending.DefaultGetTrendingMoviesUseCase
import tv.trakt.trakt.core.movies.sections.trending.usecase.trending.HalloweenGetTrendingMoviesUseCase
import tv.trakt.trakt.core.movies.ui.context.MovieContextViewModel

internal val moviesDataModule = module {

    single<MoviesRemoteDataSource> {
        MoviesApiClient(
            moviesApi = get(),
            recommendationsApi = get(),
        )
    }

    single<PopularMoviesLocalDataSource> {
        PopularMoviesStorage()
    }

    single<AnticipatedMoviesLocalDataSource> {
        AnticipatedMoviesStorage()
    }

    single<TrendingMoviesLocalDataSource> {
        TrendingMoviesStorage()
    }

    single<RecommendedMoviesLocalDataSource> {
        RecommendedMoviesStorage()
    }
}

internal val moviesModule = module {
    factory<GetTrendingMoviesUseCase>(
        qualifier = named("defaultTrendingMoviesUseCase"),
    ) {
        DefaultGetTrendingMoviesUseCase(
            remoteSource = get(),
            localTrendingSource = get(),
            localMovieSource = get(),
        )
    }

    factory<GetTrendingMoviesUseCase>(
        qualifier = named("halloweenTrendingMoviesUseCase"),
    ) {
        HalloweenGetTrendingMoviesUseCase(
            remoteSource = get(),
            localTrendingSource = get(),
            localMovieSource = get(),
        )
    }

    factory<GetPopularMoviesUseCase>(
        qualifier = named("defaultPopularMoviesUseCase"),
    ) {
        DefaultGetPopularMoviesUseCase(
            remoteSource = get(),
            localPopularSource = get(),
            localMovieSource = get(),
        )
    }

    factory<GetPopularMoviesUseCase>(
        qualifier = named("halloweenPopularMoviesUseCase"),
    ) {
        HalloweenGetPopularMoviesUseCase(
            remoteSource = get(),
            localPopularSource = get(),
            localMovieSource = get(),
        )
    }

    factory<GetAnticipatedMoviesUseCase>(
        qualifier = named("defaultAnticipatedMoviesUseCase"),
    ) {
        DefaultGetAnticipatedMoviesUseCase(
            remoteSource = get(),
            localAnticipatedSource = get(),
            localMovieSource = get(),
        )
    }

    factory<GetAnticipatedMoviesUseCase>(
        qualifier = named("halloweenAnticipatedMoviesUseCase"),
    ) {
        HalloweenGetAnticipatedMoviesUseCase(
            remoteSource = get(),
            localAnticipatedSource = get(),
            localMovieSource = get(),
        )
    }

    factory<GetRecommendedMoviesUseCase>(
        qualifier = named("defaultRecommendedMoviesUseCase"),
    ) {
        DefaultGetRecommendedMoviesUseCase(
            remoteSource = get(),
            localRecommendedSource = get(),
            localMovieSource = get(),
        )
    }

    factory<GetRecommendedMoviesUseCase>(
        qualifier = named("halloweenRecommendedMoviesUseCase"),
    ) {
        HalloweenGetRecommendedMoviesUseCase(
            remoteSource = get(),
            localRecommendedSource = get(),
            localMovieSource = get(),
        )
    }

    viewModel {
        MoviesViewModel(
            sessionManager = get(),
            analytics = get(),
        )
    }

    viewModel { (halloween: Boolean) ->
        MoviesTrendingViewModel(
            getTrendingUseCase = when {
                halloween -> get(named("halloweenTrendingMoviesUseCase"))
                else -> get(named("defaultTrendingMoviesUseCase"))
            },
        )
    }

    viewModel { (halloween: Boolean) ->
        AllMoviesTrendingViewModel(
            getTrendingUseCase = when {
                halloween -> get(named("halloweenTrendingMoviesUseCase"))
                else -> get(named("defaultTrendingMoviesUseCase"))
            },
            analytics = get(),
        )
    }

    viewModel { (halloween: Boolean) ->
        MoviesAnticipatedViewModel(
            getAnticipatedUseCase = when {
                halloween -> get(named("halloweenAnticipatedMoviesUseCase"))
                else -> get(named("defaultAnticipatedMoviesUseCase"))
            },
        )
    }

    viewModel { (halloween: Boolean) ->
        AllMoviesAnticipatedViewModel(
            getAnticipatedUseCase = when {
                halloween -> get(named("halloweenAnticipatedMoviesUseCase"))
                else -> get(named("defaultAnticipatedMoviesUseCase"))
            },
            analytics = get(),
        )
    }

    viewModel { (halloween: Boolean) ->
        MoviesRecommendedViewModel(
            getRecommendedUseCase = when {
                halloween -> get(named("halloweenRecommendedMoviesUseCase"))
                else -> get(named("defaultRecommendedMoviesUseCase"))
            },
        )
    }

    viewModel { (halloween: Boolean) ->
        AllMoviesRecommendedViewModel(
            getRecommendedUseCase = when {
                halloween -> get(named("halloweenRecommendedMoviesUseCase"))
                else -> get(named("defaultRecommendedMoviesUseCase"))
            },
            analytics = get(),
        )
    }

    viewModel { (halloween: Boolean) ->
        MoviesPopularViewModel(
            getPopularUseCase = when {
                halloween -> get(named("halloweenPopularMoviesUseCase"))
                else -> get(named("defaultPopularMoviesUseCase"))
            },
        )
    }

    viewModel { (halloween: Boolean) ->
        AllMoviesPopularViewModel(
            getPopularUseCase = when {
                halloween -> get(named("halloweenPopularMoviesUseCase"))
                else -> get(named("defaultPopularMoviesUseCase"))
            },
            analytics = get(),
        )
    }

    viewModel { (movie: Movie) ->
        MovieContextViewModel(
            movie = movie,
            updateMovieHistoryUseCase = get(),
            updateMovieWatchlistUseCase = get(),
            userProgressLocalSource = get(),
            userWatchlistLocalSource = get(),
            loadProgressUseCase = get(),
            loadWatchlistUseCase = get(),
            sessionManager = get(),
        )
    }
}

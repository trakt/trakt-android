package tv.trakt.trakt.core.movies.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.discover.sections.anticipated.data.local.movies.AnticipatedMoviesLocalDataSource
import tv.trakt.trakt.core.discover.sections.anticipated.data.local.movies.AnticipatedMoviesStorage
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.GetAnticipatedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.movies.CustomGetAnticipatedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.movies.DefaultGetAnticipatedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.popular.data.local.movies.PopularMoviesLocalDataSource
import tv.trakt.trakt.core.discover.sections.popular.data.local.movies.PopularMoviesStorage
import tv.trakt.trakt.core.discover.sections.popular.usecases.GetPopularMoviesUseCase
import tv.trakt.trakt.core.discover.sections.popular.usecases.movies.CustomGetPopularMoviesUseCase
import tv.trakt.trakt.core.discover.sections.popular.usecases.movies.DefaultGetPopularMoviesUseCase
import tv.trakt.trakt.core.discover.sections.recommended.data.local.movies.RecommendedMoviesLocalDataSource
import tv.trakt.trakt.core.discover.sections.recommended.data.local.movies.RecommendedMoviesStorage
import tv.trakt.trakt.core.discover.sections.recommended.usecase.GetRecommendedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.recommended.usecase.movies.CustomGetRecommendedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.recommended.usecase.movies.DefaultGetRecommendedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.trending.data.local.movies.TrendingMoviesLocalDataSource
import tv.trakt.trakt.core.discover.sections.trending.data.local.movies.TrendingMoviesStorage
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingMoviesUseCase
import tv.trakt.trakt.core.discover.sections.trending.usecases.movies.CustomGetTrendingMoviesUseCase
import tv.trakt.trakt.core.discover.sections.trending.usecases.movies.DefaultGetTrendingMoviesUseCase
import tv.trakt.trakt.core.movies.data.remote.MoviesApiClient
import tv.trakt.trakt.core.movies.data.remote.MoviesRemoteDataSource
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

    single<RecommendedMoviesLocalDataSource> {
        RecommendedMoviesStorage()
    }

    single<AnticipatedMoviesLocalDataSource> {
        AnticipatedMoviesStorage()
    }

    single<TrendingMoviesLocalDataSource> {
        TrendingMoviesStorage()
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
        qualifier = named("customTrendingMoviesUseCase"),
    ) {
        CustomGetTrendingMoviesUseCase(
            remoteSource = get(),
            localTrendingSource = get(),
            localMovieSource = get(),
            customThemeUseCase = get(),
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
        qualifier = named("customPopularMoviesUseCase"),
    ) {
        CustomGetPopularMoviesUseCase(
            remoteSource = get(),
            localPopularSource = get(),
            localMovieSource = get(),
            customThemeUseCase = get(),
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
        qualifier = named("customAnticipatedMoviesUseCase"),
    ) {
        CustomGetAnticipatedMoviesUseCase(
            remoteSource = get(),
            localAnticipatedSource = get(),
            localMovieSource = get(),
            customThemeUseCase = get(),
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
        qualifier = named("customRecommendedMoviesUseCase"),
    ) {
        CustomGetRecommendedMoviesUseCase(
            remoteSource = get(),
            localRecommendedSource = get(),
            localMovieSource = get(),
            customThemeUseCase = get(),
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
            analytics = get(),
        )
    }
}

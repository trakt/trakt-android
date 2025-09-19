package tv.trakt.trakt.core.movies.di

import org.koin.core.module.dsl.viewModel
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
import tv.trakt.trakt.core.movies.sections.popular.MoviesPopularViewModel
import tv.trakt.trakt.core.movies.sections.popular.all.AllMoviesPopularViewModel
import tv.trakt.trakt.core.movies.sections.popular.data.local.PopularMoviesLocalDataSource
import tv.trakt.trakt.core.movies.sections.popular.data.local.PopularMoviesStorage
import tv.trakt.trakt.core.movies.sections.popular.usecase.GetPopularMoviesUseCase
import tv.trakt.trakt.core.movies.sections.recommended.MoviesRecommendedViewModel
import tv.trakt.trakt.core.movies.sections.recommended.all.AllMoviesRecommendedViewModel
import tv.trakt.trakt.core.movies.sections.recommended.data.local.RecommendedMoviesLocalDataSource
import tv.trakt.trakt.core.movies.sections.recommended.data.local.RecommendedMoviesStorage
import tv.trakt.trakt.core.movies.sections.recommended.usecase.GetRecommendedMoviesUseCase
import tv.trakt.trakt.core.movies.sections.trending.MoviesTrendingViewModel
import tv.trakt.trakt.core.movies.sections.trending.all.AllMoviesTrendingViewModel
import tv.trakt.trakt.core.movies.sections.trending.data.local.TrendingMoviesLocalDataSource
import tv.trakt.trakt.core.movies.sections.trending.data.local.TrendingMoviesStorage
import tv.trakt.trakt.core.movies.sections.trending.usecase.GetTrendingMoviesUseCase
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
    factory {
        GetTrendingMoviesUseCase(
            remoteSource = get(),
            localTrendingSource = get(),
        )
    }

    factory {
        GetPopularMoviesUseCase(
            remoteSource = get(),
            localPopularSource = get(),
        )
    }

    factory {
        GetAnticipatedMoviesUseCase(
            remoteSource = get(),
            localAnticipatedSource = get(),
        )
    }

    factory {
        GetRecommendedMoviesUseCase(
            remoteSource = get(),
            localRecommendedSource = get(),
        )
    }

    viewModel {
        MoviesViewModel(
            sessionManager = get(),
        )
    }

    viewModel {
        MoviesTrendingViewModel(
            getTrendingUseCase = get(),
        )
    }

    viewModel {
        AllMoviesTrendingViewModel(
            getTrendingUseCase = get(),
        )
    }

    viewModel {
        AllMoviesAnticipatedViewModel(
            getAnticipatedUseCase = get(),
        )
    }

    viewModel {
        MoviesAnticipatedViewModel(
            getAnticipatedUseCase = get(),
        )
    }

    viewModel {
        AllMoviesAnticipatedViewModel(
            getAnticipatedUseCase = get(),
        )
    }

    viewModel {
        MoviesRecommendedViewModel(
            getRecommendedUseCase = get(),
        )
    }

    viewModel {
        AllMoviesRecommendedViewModel(
            getRecommendedUseCase = get(),
        )
    }

    viewModel {
        MoviesPopularViewModel(
            getPopularUseCase = get(),
        )
    }

    viewModel {
        AllMoviesPopularViewModel(
            getPopularUseCase = get(),
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
        )
    }
}

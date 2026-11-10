package tv.trakt.trakt.core.movies.di

import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.discover.sections.anticipated.usecases.GetAnticipatedMoviesUseCase
import tv.trakt.trakt.core.discover.sections.popular.usecases.GetPopularMoviesUseCase
import tv.trakt.trakt.core.discover.sections.releases.usecases.movies.GetReleasesMoviesUseCase
import tv.trakt.trakt.core.discover.sections.trending.usecases.GetTrendingMoviesUseCase
import tv.trakt.trakt.core.home.sections.recommended.usecase.GetRecommendedMoviesUseCase
import tv.trakt.trakt.core.home.sections.recommended.usecase.HideRecommendedMovieUseCase
import tv.trakt.trakt.core.movies.ui.context.MovieContextViewModel

internal val moviesModule = module {
    factory(
        qualifier = named("defaultTrendingMoviesUseCase"),
    ) {
        GetTrendingMoviesUseCase(
            remoteSource = get(),
            localTrendingSource = get(),
            localMovieSource = get(),
            seasonalThemeUseCase = get(),
        )
    }

    factory(
        qualifier = named("defaultPopularMoviesUseCase"),
    ) {
        GetPopularMoviesUseCase(
            remoteSource = get(),
            localPopularSource = get(),
            localMovieSource = get(),
            seasonalThemeUseCase = get(),
        )
    }

    factory(
        qualifier = named("defaultAnticipatedMoviesUseCase"),
    ) {
        GetAnticipatedMoviesUseCase(
            remoteSource = get(),
            localAnticipatedSource = get(),
            localMovieSource = get(),
            seasonalThemeUseCase = get(),
        )
    }

    factory(
        qualifier = named("defaultReleasesMoviesUseCase"),
    ) {
        GetReleasesMoviesUseCase(
            remoteSource = get(),
            localSource = get(),
            localMovieSource = get(),
        )
    }

    factory(
        qualifier = named("defaultRecommendedMoviesUseCase"),
    ) {
        GetRecommendedMoviesUseCase(
            remoteSource = get(),
            localRecommendedSource = get(),
            localMovieSource = get(),
        )
    }

    factoryOf(::HideRecommendedMovieUseCase)

    viewModel { (movie: Movie) ->
        MovieContextViewModel(
            appContext = androidApplication(),
            movie = movie,
            updateMovieHistoryUseCase = get(),
            updateMovieWatchlistUseCase = get(),
            userProgressLocalSource = get(),
            userWatchlistLocalSource = get(),
            userWatchlistMinLocalSource = get(),
            loadProgressUseCase = get(),
            loadWatchlistUseCase = get(),
            sessionManager = get(),
            checkInManager = get(),
            ratePromptManager = get(),
            analytics = get(),
            errorsManager = get(),
            dataChanges = get(),
        )
    }
}

package tv.trakt.trakt.core.summary.movies.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieStorage
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.summary.movies.MovieDetailsViewModel
import tv.trakt.trakt.core.summary.movies.data.local.MovieRatingsLocalDataSource
import tv.trakt.trakt.core.summary.movies.data.local.MovieRatingsStorage
import tv.trakt.trakt.core.summary.movies.features.context.lists.MovieDetailsListsViewModel
import tv.trakt.trakt.core.summary.movies.features.context.more.MovieDetailsContextViewModel
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieDetailsUseCase
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieRatingsUseCase
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieStreamingsUseCase
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieStudiosUseCase

internal val movieDetailsDataModule = module {
    single<MovieLocalDataSource> {
        MovieStorage()
    }

    single<MovieRatingsLocalDataSource> {
        MovieRatingsStorage()
    }
}

internal val movieDetailsModule = module {
    factory {
        GetMovieDetailsUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        GetMovieRatingsUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        GetMovieStudiosUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetMovieStreamingsUseCase(
            remoteMovieSource = get(),
            remoteStreamingSource = get(),
            localStreamingSource = get(),
            priorityStreamingProvider = get(),
        )
    }

    viewModel {
        MovieDetailsViewModel(
            savedStateHandle = get(),
            getDetailsUseCase = get(),
            getExternalRatingsUseCase = get(),
            getMovieStudiosUseCase = get(),
            loadProgressUseCase = get(),
            loadWatchlistUseCase = get(),
            loadListsUseCase = get(),
            updateMovieHistoryUseCase = get(),
            updateMovieWatchlistUseCase = get(),
            addListItemUseCase = get(),
            removeListItemUseCase = get(),
            userWatchlistLocalSource = get(),
            sessionManager = get(),
        )
    }

    viewModel { (movie: Movie) ->
        MovieDetailsContextViewModel(
            movie = movie,
            sessionManager = get(),
            getStreamingsUseCase = get(),
        )
    }

    viewModel { (movie: Movie) ->
        MovieDetailsListsViewModel(
            movie = movie,
            sessionManager = get(),
            loadListsUseCase = get(),
        )
    }
}

package tv.trakt.trakt.core.summary.movies.di

import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieStorage
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.summary.movies.MovieDetailsViewModel
import tv.trakt.trakt.core.summary.movies.data.MovieDetailsUpdates
import tv.trakt.trakt.core.summary.movies.data.MovieDetailsUpdatesStorage
import tv.trakt.trakt.core.summary.movies.features.actors.MovieActorsViewModel
import tv.trakt.trakt.core.summary.movies.features.actors.usecases.GetMovieActorsUseCase
import tv.trakt.trakt.core.summary.movies.features.comments.MovieCommentsViewModel
import tv.trakt.trakt.core.summary.movies.features.comments.usecases.GetMovieCommentsUseCase
import tv.trakt.trakt.core.summary.movies.features.context.lists.MovieDetailsListsViewModel
import tv.trakt.trakt.core.summary.movies.features.context.more.MovieDetailsContextViewModel
import tv.trakt.trakt.core.summary.movies.features.extras.MovieExtrasViewModel
import tv.trakt.trakt.core.summary.movies.features.extras.usecases.GetMovieExtrasUseCase
import tv.trakt.trakt.core.summary.movies.features.history.MovieHistoryViewModel
import tv.trakt.trakt.core.summary.movies.features.history.usecases.GetMovieHistoryUseCase
import tv.trakt.trakt.core.summary.movies.features.lists.MovieListsViewModel
import tv.trakt.trakt.core.summary.movies.features.lists.usecases.GetMovieListsUseCase
import tv.trakt.trakt.core.summary.movies.features.related.MovieRelatedViewModel
import tv.trakt.trakt.core.summary.movies.features.related.usecases.GetMovieRelatedUseCase
import tv.trakt.trakt.core.summary.movies.features.sentiment.MovieSentimentViewModel
import tv.trakt.trakt.core.summary.movies.features.sentiment.usecases.GetMovieSentimentUseCase
import tv.trakt.trakt.core.summary.movies.features.streaming.MovieStreamingsViewModel
import tv.trakt.trakt.core.summary.movies.features.streaming.usecases.GetMovieStreamingsUseCase
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieDetailsUseCase
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieRatingsUseCase
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieStreamingUseCase
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieStudiosUseCase

internal val movieDetailsDataModule = module {
    single<MovieLocalDataSource> {
        MovieStorage()
    }

    single<MovieDetailsUpdates> {
        MovieDetailsUpdatesStorage()
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
        )
    }

    factory {
        GetMovieStudiosUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetMovieExtrasUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetMovieActorsUseCase(
            remoteSource = get(),
            peopleLocalSource = get(),
        )
    }

    factory {
        GetMovieRelatedUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        GetMovieStreamingsUseCase(
            remoteMovieSource = get(),
            remoteStreamingSource = get(),
            localStreamingSource = get(),
        )
    }

    factory {
        GetMovieStreamingUseCase(
            remoteMovieSource = get(),
            remoteStreamingSource = get(),
            localStreamingSource = get(),
            priorityStreamingProvider = get(),
        )
    }

    factory {
        GetMovieSentimentUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetMovieHistoryUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetMovieCommentsUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetMovieListsUseCase(
            remoteSource = get(),
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
            movieDetailsUpdates = get(),
            sessionManager = get(),
        )
    }

    viewModel { (movie: Movie) ->
        MovieStreamingsViewModel(
            movie = movie,
            sessionManager = get(),
            getStreamingsUseCase = get(),
        )
    }

    viewModel { (movie: Movie) ->
        MovieExtrasViewModel(
            movie = movie,
            getExtrasUseCase = get(),
        )
    }

    viewModel { (movie: Movie) ->
        MovieActorsViewModel(
            movie = movie,
            getActorsUseCase = get(),
        )
    }

    viewModel { (movie: Movie) ->
        MovieRelatedViewModel(
            movie = movie,
            getRelatedMoviesUseCase = get(),
        )
    }

    viewModel { (movie: Movie) ->
        MovieListsViewModel(
            movie = movie,
            getListsUseCase = get(),
        )
    }

    viewModel { (movie: Movie) ->
        MovieHistoryViewModel(
            movie = movie,
            getHistoryUseCase = get(),
            movieDetailsUpdates = get(),
        )
    }

    viewModel { (movie: Movie) ->
        MovieSentimentViewModel(
            movie = movie,
            getSentimentUseCase = get(),
        )
    }

    viewModel { (movie: Movie) ->
        MovieCommentsViewModel(
            appContext = androidApplication(),
            movie = movie,
            getCommentsUseCase = get(),
            getCommentReactionsUseCase = get(),
            loadUserReactionsUseCase = get(),
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

package tv.trakt.trakt.app.core.details.movie.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.app.core.details.movie.MovieDetailsViewModel
import tv.trakt.trakt.app.core.details.movie.usecases.GetCastCrewUseCase
import tv.trakt.trakt.app.core.details.movie.usecases.GetCommentsUseCase
import tv.trakt.trakt.app.core.details.movie.usecases.GetCustomListsUseCase
import tv.trakt.trakt.app.core.details.movie.usecases.GetExternalRatingsUseCase
import tv.trakt.trakt.app.core.details.movie.usecases.GetExtraVideosUseCase
import tv.trakt.trakt.app.core.details.movie.usecases.GetMovieDetailsUseCase
import tv.trakt.trakt.app.core.details.movie.usecases.GetRelatedMoviesUseCase
import tv.trakt.trakt.app.core.details.movie.usecases.collection.ChangeHistoryUseCase
import tv.trakt.trakt.app.core.details.movie.usecases.collection.ChangeWatchlistUseCase
import tv.trakt.trakt.app.core.details.movie.usecases.collection.GetCollectionUseCase
import tv.trakt.trakt.app.core.details.movie.usecases.streamings.GetPlexUseCase
import tv.trakt.trakt.app.core.details.movie.usecases.streamings.GetStreamingsUseCase

internal val movieDetailsModule = module {
    factory {
        GetMovieDetailsUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        GetExternalRatingsUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetExtraVideosUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetCastCrewUseCase(
            remoteSource = get(),
            peopleLocalSource = get(),
        )
    }

    factory {
        GetRelatedMoviesUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        GetCommentsUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetCustomListsUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetStreamingsUseCase(
            remoteMovieSource = get(),
            remoteStreamingSource = get(),
            localStreamingSource = get(),
            priorityStreamingProvider = get(),
        )
    }

    factory {
        GetPlexUseCase(
            remoteSyncSource = get(),
            remoteMovieSource = get(),
            localMovieSource = get(),
        )
    }

    factory {
        GetCollectionUseCase(
            remoteSource = get(),
            syncLocalSource = get(),
        )
    }

    factory {
        ChangeWatchlistUseCase(
            remoteSource = get(),
            syncLocalSource = get(),
        )
    }

    factory {
        ChangeHistoryUseCase(
            remoteSource = get(),
            syncLocalSource = get(),
        )
    }

    viewModel {
        MovieDetailsViewModel(
            savedStateHandle = get(),
            getDetailsUseCase = get(),
            getExternalRatingsUseCase = get(),
            getExtraVideosUseCase = get(),
            getCastCrewUseCase = get(),
            getRelatedMoviesUseCase = get(),
            getCommentsUseCase = get(),
            getListsUseCase = get(),
            getStreamingsUseCase = get(),
            getPlexUseCase = get(),
            getCollectionUseCase = get(),
            watchlistUseCase = get(),
            historyUseCase = get(),
            appReviewUseCase = get(),
            sessionManager = get(),
            tutorialsManager = get(),
        )
    }
}

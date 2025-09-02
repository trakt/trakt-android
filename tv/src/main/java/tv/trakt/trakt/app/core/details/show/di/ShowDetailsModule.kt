package tv.trakt.trakt.app.core.details.show.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.app.core.details.show.ShowDetailsViewModel
import tv.trakt.trakt.app.core.details.show.usecases.GetCastCrewUseCase
import tv.trakt.trakt.app.core.details.show.usecases.GetCommentsUseCase
import tv.trakt.trakt.app.core.details.show.usecases.GetCustomListsUseCase
import tv.trakt.trakt.app.core.details.show.usecases.GetExternalRatingsUseCase
import tv.trakt.trakt.app.core.details.show.usecases.GetExtraVideosUseCase
import tv.trakt.trakt.app.core.details.show.usecases.GetRelatedShowsUseCase
import tv.trakt.trakt.app.core.details.show.usecases.GetShowDetailsUseCase
import tv.trakt.trakt.app.core.details.show.usecases.GetShowSeasonsUseCase
import tv.trakt.trakt.app.core.details.show.usecases.collection.ChangeHistoryUseCase
import tv.trakt.trakt.app.core.details.show.usecases.collection.ChangeWatchlistUseCase
import tv.trakt.trakt.app.core.details.show.usecases.collection.GetCollectionUseCase
import tv.trakt.trakt.app.core.details.show.usecases.streamings.GetPlexUseCase
import tv.trakt.trakt.app.core.details.show.usecases.streamings.GetStreamingsUseCase

internal val showDetailsModule = module {
    factory {
        GetShowDetailsUseCase(
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
        GetRelatedShowsUseCase(
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
            remoteShowSource = get(),
            remoteStreamingSource = get(),
            localStreamingSource = get(),
            priorityStreamingProvider = get(),
        )
    }

    factory {
        GetPlexUseCase(
            remoteSyncSource = get(),
            remoteShowSource = get(),
            localShowSource = get(),
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

    factory {
        GetShowSeasonsUseCase(
            remoteShowsSource = get(),
            remoteEpisodesSource = get(),
            localEpisodeSource = get(),
        )
    }

    viewModel {
        ShowDetailsViewModel(
            savedStateHandle = get(),
            getDetailsUseCase = get(),
            getExternalRatingsUseCase = get(),
            getExtraVideosUseCase = get(),
            getCastCrewUseCase = get(),
            getRelatedShowsUseCase = get(),
            getCommentsUseCase = get(),
            getListsUseCase = get(),
            getStreamingsUseCase = get(),
            getPlexUseCase = get(),
            getCollectionUseCase = get(),
            getSeasonsUseCase = get(),
            watchlistUseCase = get(),
            historyUseCase = get(),
            sessionManager = get(),
            tutorialsManager = get(),
        )
    }
}

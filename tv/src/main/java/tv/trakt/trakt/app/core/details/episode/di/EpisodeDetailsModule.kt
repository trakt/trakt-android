package tv.trakt.trakt.app.core.details.episode.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.app.core.details.episode.EpisodeDetailsViewModel
import tv.trakt.trakt.app.core.details.episode.usecases.GetCastCrewUseCase
import tv.trakt.trakt.app.core.details.episode.usecases.GetCommentsUseCase
import tv.trakt.trakt.app.core.details.episode.usecases.GetEpisodeDetailsUseCase
import tv.trakt.trakt.app.core.details.episode.usecases.GetEpisodeHistoryUseCase
import tv.trakt.trakt.app.core.details.episode.usecases.GetEpisodeSeasonUseCase
import tv.trakt.trakt.app.core.details.episode.usecases.GetExternalRatingsUseCase
import tv.trakt.trakt.app.core.details.episode.usecases.GetStreamingsUseCase
import tv.trakt.trakt.app.core.details.episode.usecases.collection.ChangeHistoryUseCase

internal val episodeDetailsModule = module {

    factory {
        GetEpisodeDetailsUseCase(
            localSource = get(),
            remoteSource = get(),
        )
    }

    factory {
        GetExternalRatingsUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetStreamingsUseCase(
            remoteEpisodesSource = get(),
            remoteStreamingSource = get(),
            localStreamingSource = get(),
            priorityStreamingProvider = get(),
        )
    }

    factory {
        GetCastCrewUseCase(
            remoteSource = get(),
            peopleLocalSource = get(),
        )
    }

    factory {
        GetCommentsUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetEpisodeSeasonUseCase(
            remoteSource = get(),
            localEpisodeSource = get(),
        )
    }

    factory {
        GetEpisodeHistoryUseCase(
            remoteSource = get(),
            syncLocalSource = get(),
        )
    }

    factory {
        ChangeHistoryUseCase(
            remoteSource = get(),
        )
    }

    viewModel {
        EpisodeDetailsViewModel(
            savedStateHandle = get(),
            sessionManager = get(),
            tutorialsManager = get(),
            getShowDetailsUseCase = get(),
            getEpisodeDetailsUseCase = get(),
            getExternalRatingsUseCase = get(),
            getStreamingsUseCase = get(),
            getCastCrewUseCase = get(),
            getRelatedShowsUseCase = get(),
            getCommentsUseCase = get(),
            getSeasonUseCase = get(),
            getHistoryUseCase = get(),
            changeHistoryUseCase = get(),
            appReviewUseCase = get(),
        )
    }
}

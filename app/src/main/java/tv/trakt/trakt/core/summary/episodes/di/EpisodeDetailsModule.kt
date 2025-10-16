package tv.trakt.trakt.core.summary.episodes.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeStorage
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.episodes.EpisodeDetailsViewModel
import tv.trakt.trakt.core.summary.episodes.features.actors.EpisodeActorsViewModel
import tv.trakt.trakt.core.summary.episodes.features.actors.usecases.GetEpisodeActorsUseCase
import tv.trakt.trakt.core.summary.episodes.features.streaming.EpisodeStreamingsViewModel
import tv.trakt.trakt.core.summary.episodes.features.streaming.usecases.GetEpisodeStreamingsUseCase
import tv.trakt.trakt.core.summary.episodes.usecases.GetEpisodeDetailsUseCase
import tv.trakt.trakt.core.summary.episodes.usecases.GetEpisodeRatingsUseCase

internal val episodeDetailsDataModule = module {
    single<EpisodeLocalDataSource> {
        EpisodeStorage()
    }
}

internal val episodeDetailsModule = module {
    factory {
        GetEpisodeDetailsUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        GetEpisodeRatingsUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetEpisodeActorsUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetEpisodeStreamingsUseCase(
            remoteEpisodeSource = get(),
            remoteStreamingSource = get(),
            localStreamingSource = get(),
        )
    }

    viewModel {
        EpisodeDetailsViewModel(
            savedStateHandle = get(),
            getShowDetailsUseCase = get(),
            getEpisodeDetailsUseCase = get(),
            getRatingsUseCase = get(),
            sessionManager = get(),
        )
    }

    viewModel { (show: Show, episode: Episode) ->
        EpisodeActorsViewModel(
            show = show,
            episode = episode,
            getActorsUseCase = get(),
        )
    }

    viewModel { (show: Show, episode: Episode) ->
        EpisodeStreamingsViewModel(
            show = show,
            episode = episode,
            sessionManager = get(),
            getStreamingsUseCase = get(),
        )
    }
}

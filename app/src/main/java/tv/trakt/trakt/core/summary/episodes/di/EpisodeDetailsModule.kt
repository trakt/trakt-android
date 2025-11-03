package tv.trakt.trakt.core.summary.episodes.di

import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeStorage
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.episodes.EpisodeDetailsViewModel
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdatesStorage
import tv.trakt.trakt.core.summary.episodes.features.actors.EpisodeActorsViewModel
import tv.trakt.trakt.core.summary.episodes.features.actors.usecases.GetEpisodeActorsUseCase
import tv.trakt.trakt.core.summary.episodes.features.comments.EpisodeCommentsViewModel
import tv.trakt.trakt.core.summary.episodes.features.comments.usecases.GetEpisodeCommentsUseCase
import tv.trakt.trakt.core.summary.episodes.features.context.more.EpisodeDetailsContextViewModel
import tv.trakt.trakt.core.summary.episodes.features.history.EpisodeHistoryViewModel
import tv.trakt.trakt.core.summary.episodes.features.history.usecases.GetEpisodeHistoryUseCase
import tv.trakt.trakt.core.summary.episodes.features.related.EpisodeRelatedViewModel
import tv.trakt.trakt.core.summary.episodes.features.related.usecases.GetEpisodeRelatedUseCase
import tv.trakt.trakt.core.summary.episodes.features.season.EpisodeSeasonViewModel
import tv.trakt.trakt.core.summary.episodes.features.season.usecases.GetEpisodeSeasonUseCase
import tv.trakt.trakt.core.summary.episodes.features.streaming.EpisodeStreamingsViewModel
import tv.trakt.trakt.core.summary.episodes.features.streaming.usecases.GetEpisodeStreamingsUseCase
import tv.trakt.trakt.core.summary.episodes.usecases.GetEpisodeDetailsUseCase
import tv.trakt.trakt.core.summary.episodes.usecases.GetEpisodeRatingsUseCase
import tv.trakt.trakt.core.summary.episodes.usecases.GetEpisodeStreamingUseCase

internal val episodeDetailsDataModule = module {
    single<EpisodeLocalDataSource> {
        EpisodeStorage()
    }

    single<EpisodeDetailsUpdates> {
        EpisodeDetailsUpdatesStorage()
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
            peopleLocalSource = get(),
        )
    }

    factory {
        GetEpisodeCommentsUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetEpisodeHistoryUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetEpisodeSeasonUseCase(
            remoteEpisodesSource = get(),
        )
    }

    factory {
        GetEpisodeRelatedUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        GetEpisodeStreamingsUseCase(
            remoteEpisodeSource = get(),
            remoteStreamingSource = get(),
            localStreamingSource = get(),
        )
    }

    factory {
        GetEpisodeStreamingUseCase(
            remoteEpisodeSource = get(),
            remoteStreamingSource = get(),
            localStreamingSource = get(),
            priorityStreamingProvider = get(),
        )
    }

    viewModel {
        EpisodeDetailsViewModel(
            savedStateHandle = get(),
            getShowDetailsUseCase = get(),
            getEpisodeDetailsUseCase = get(),
            getRatingsUseCase = get(),
            loadProgressUseCase = get(),
            updateHistoryUseCase = get(),
            showUpdatesSource = get(),
            episodeUpdatesSource = get(),
            episodeLocalDataSource = get(),
            sessionManager = get(),
            analytics = get(),
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
        EpisodeCommentsViewModel(
            appContext = androidApplication(),
            show = show,
            episode = episode,
            getFilterUseCase = get(),
            getCommentsUseCase = get(),
            getCommentReactionsUseCase = get(),
            loadUserReactionsUseCase = get(),
            reactionsUpdates = get(),
            sessionManager = get(),
        )
    }

    viewModel { (episode: Episode) ->
        EpisodeHistoryViewModel(
            episode = episode,
            getHistoryUseCase = get(),
            showUpdatesSource = get(),
            episodeUpdatesSource = get(),
        )
    }

    viewModel { (show: Show, episode: Episode) ->
        EpisodeSeasonViewModel(
            show = show,
            episode = episode,
            getSeasonDetailsUseCase = get(),
            loadUserProgressUseCase = get(),
            updateEpisodeHistoryUseCase = get(),
            showDetailsUpdates = get(),
            episodeDetailsUpdates = get(),
            sessionManager = get(),
            analytics = get(),
        )
    }

    viewModel { (show: Show) ->
        EpisodeRelatedViewModel(
            show = show,
            getRelatedShowsUseCase = get(),
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

    viewModel { (show: Show, episode: Episode) ->
        EpisodeDetailsContextViewModel(
            show = show,
            episode = episode,
            sessionManager = get(),
            getStreamingsUseCase = get(),
        )
    }
}

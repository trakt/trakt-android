package tv.trakt.trakt.core.summary.episodes.di

import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
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
import tv.trakt.trakt.core.summary.episodes.features.actors.usecases.GetEpisodeDirectorUseCase
import tv.trakt.trakt.core.summary.episodes.features.comments.EpisodeCommentsViewModel
import tv.trakt.trakt.core.summary.episodes.features.comments.usecases.GetEpisodeCommentsUseCase
import tv.trakt.trakt.core.summary.episodes.features.context.more.EpisodeDetailsContextViewModel
import tv.trakt.trakt.core.summary.episodes.features.history.EpisodeHistoryViewModel
import tv.trakt.trakt.core.summary.episodes.features.history.usecases.GetEpisodeHistoryUseCase
import tv.trakt.trakt.core.summary.episodes.features.info.EpisodeInfoViewModel
import tv.trakt.trakt.core.summary.episodes.features.info.usecase.GetEpisodeCrewUseCase
import tv.trakt.trakt.core.summary.episodes.features.info.usecase.GetEpisodeStatsUseCase
import tv.trakt.trakt.core.summary.episodes.features.related.EpisodeRelatedViewModel
import tv.trakt.trakt.core.summary.episodes.features.related.usecases.GetEpisodeRelatedUseCase
import tv.trakt.trakt.core.summary.episodes.features.season.EpisodeSeasonViewModel
import tv.trakt.trakt.core.summary.episodes.features.season.usecases.GetEpisodeSeasonUseCase
import tv.trakt.trakt.core.summary.episodes.features.socials.GetEpisodeSocialsUseCase
import tv.trakt.trakt.core.summary.episodes.features.streaming.EpisodeStreamingsViewModel
import tv.trakt.trakt.core.summary.episodes.features.streaming.usecases.GetEpisodeStreamingsUseCase
import tv.trakt.trakt.core.summary.episodes.usecases.GetEpisodeDetailsUseCase
import tv.trakt.trakt.core.summary.episodes.usecases.GetEpisodeRatingsUseCase
import tv.trakt.trakt.core.summary.episodes.usecases.GetEpisodeStreamingUseCase

internal val episodeDetailsDataModule = module {
    singleOf(::EpisodeStorage) { bind<EpisodeLocalDataSource>() }

    singleOf(::EpisodeDetailsUpdatesStorage) { bind<EpisodeDetailsUpdates>() }
}

internal val episodeDetailsModule = module {
    factoryOf(::GetEpisodeDetailsUseCase)
    factoryOf(::GetEpisodeRatingsUseCase)
    factoryOf(::GetEpisodeActorsUseCase)
    factoryOf(::GetEpisodeDirectorUseCase)
    factoryOf(::GetEpisodeCommentsUseCase)
    factoryOf(::GetEpisodeHistoryUseCase)
    factoryOf(::GetEpisodeSeasonUseCase)
    factoryOf(::GetEpisodeRelatedUseCase)
    factoryOf(::GetEpisodeStreamingsUseCase)
    factoryOf(::GetEpisodeStreamingUseCase)
    factoryOf(::GetEpisodeStatsUseCase)
    factoryOf(::GetEpisodeCrewUseCase)
    factoryOf(::GetEpisodeSocialsUseCase)

    viewModel {
        EpisodeDetailsViewModel(
            appContext = androidApplication(),
            savedStateHandle = get(),
            getShowDetailsUseCase = get(),
            getEpisodeDetailsUseCase = get(),
            getEpisodeDirectorUseCase = get(),
            getEpisodeTranslationsUseCase = get(),
            getEpisodeSocialsUseCase = get(),
            getRatingsUseCase = get(),
            loadProgressUseCase = get(),
            loadRatingUseCase = get(),
            updateHistoryUseCase = get(),
            showUpdatesSource = get(),
            episodeUpdatesSource = get(),
            episodeLocalDataSource = get(),
            sessionManager = get(),
            checkInManager = get(),
            checkInUpdates = get(),
            analytics = get(),
        )
    }

    viewModel { (show: Show, episode: Episode) ->
        EpisodeInfoViewModel(
            show = show,
            episode = episode,
            getStatsUseCase = get(),
            getCrewUseCase = get(),
        )
    }

    viewModel { (show: Show, episode: Episode) ->
        EpisodeActorsViewModel(
            show = show,
            episode = episode,
            getActorsUseCase = get(),
            collapsingManager = get(),
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
            commentsUpdates = get(),
            collapsingManager = get(),
        )
    }

    viewModelOf(::EpisodeHistoryViewModel)

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
            collapsingManager = get(),
        )
    }

    viewModel { (show: Show) ->
        EpisodeRelatedViewModel(
            show = show,
            getRelatedShowsUseCase = get(),
            collectionStateProvider = get(),
            collapsingManager = get(),
        )
    }

    viewModel { (show: Show, episode: Episode) ->
        EpisodeStreamingsViewModel(
            show = show,
            episode = episode,
            sessionManager = get(),
            getStreamingsUseCase = get(),
            collapsingManager = get(),
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

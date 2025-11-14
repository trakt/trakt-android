package tv.trakt.trakt.core.summary.shows.di

import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowStorage
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.shows.ShowDetailsViewModel
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdatesStorage
import tv.trakt.trakt.core.summary.shows.features.actors.ShowActorsViewModel
import tv.trakt.trakt.core.summary.shows.features.actors.usecases.GetShowActorsUseCase
import tv.trakt.trakt.core.summary.shows.features.comments.ShowCommentsViewModel
import tv.trakt.trakt.core.summary.shows.features.comments.usecases.GetShowCommentsUseCase
import tv.trakt.trakt.core.summary.shows.features.context.lists.ShowDetailsListsViewModel
import tv.trakt.trakt.core.summary.shows.features.context.more.ShowDetailsContextViewModel
import tv.trakt.trakt.core.summary.shows.features.extras.ShowExtrasViewModel
import tv.trakt.trakt.core.summary.shows.features.extras.usecases.GetShowExtrasUseCase
import tv.trakt.trakt.core.summary.shows.features.history.ShowHistoryViewModel
import tv.trakt.trakt.core.summary.shows.features.history.usecases.GetShowHistoryUseCase
import tv.trakt.trakt.core.summary.shows.features.lists.ShowListsViewModel
import tv.trakt.trakt.core.summary.shows.features.lists.usecases.GetShowListsUseCase
import tv.trakt.trakt.core.summary.shows.features.related.ShowRelatedViewModel
import tv.trakt.trakt.core.summary.shows.features.related.usecases.GetShowRelatedUseCase
import tv.trakt.trakt.core.summary.shows.features.seasons.ShowSeasonsViewModel
import tv.trakt.trakt.core.summary.shows.features.seasons.usecases.GetShowSeasonsUseCase
import tv.trakt.trakt.core.summary.shows.features.sentiment.ShowSentimentViewModel
import tv.trakt.trakt.core.summary.shows.features.sentiment.usecases.GetShowSentimentUseCase
import tv.trakt.trakt.core.summary.shows.features.streaming.ShowStreamingsViewModel
import tv.trakt.trakt.core.summary.shows.features.streaming.usecases.GetShowStreamingsUseCase
import tv.trakt.trakt.core.summary.shows.usecases.GetShowDetailsUseCase
import tv.trakt.trakt.core.summary.shows.usecases.GetShowRatingsUseCase
import tv.trakt.trakt.core.summary.shows.usecases.GetShowStreamingUseCase
import tv.trakt.trakt.core.summary.shows.usecases.GetShowStudiosUseCase

internal val showDetailsDataModule = module {
    single<ShowLocalDataSource> {
        ShowStorage()
    }

    single<ShowDetailsUpdates> {
        ShowDetailsUpdatesStorage()
    }
}

internal val showDetailsModule = module {
    factory {
        GetShowDetailsUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        GetShowRatingsUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetShowStudiosUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetShowSeasonsUseCase(
            remoteShowsSource = get(),
            remoteEpisodesSource = get(),
        )
    }

    factory {
        GetShowExtrasUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetShowActorsUseCase(
            remoteSource = get(),
            peopleLocalSource = get(),
        )
    }

    factory {
        GetShowRelatedUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        GetShowStreamingsUseCase(
            remoteShowSource = get(),
            remoteStreamingSource = get(),
            localStreamingSource = get(),
        )
    }

    factory {
        GetShowStreamingUseCase(
            remoteShowSource = get(),
            remoteStreamingSource = get(),
            localStreamingSource = get(),
            priorityStreamingProvider = get(),
        )
    }

    factory {
        GetShowSentimentUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetShowCommentsUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetShowListsUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetShowHistoryUseCase(
            remoteSource = get(),
        )
    }

    viewModel {
        ShowDetailsViewModel(
            appContext = androidApplication(),
            savedStateHandle = get(),
            getDetailsUseCase = get(),
            getExternalRatingsUseCase = get(),
            getShowStudiosUseCase = get(),
            loadProgressUseCase = get(),
            loadWatchlistUseCase = get(),
            loadListsUseCase = get(),
            loadRatingUseCase = get(),
            loadFavoritesUseCase = get(),
            updateShowHistoryUseCase = get(),
            updateEpisodeHistoryUseCase = get(),
            updateShowWatchlistUseCase = get(),
            updateShowFavoritesUseCase = get(),
            addListItemUseCase = get(),
            removeListItemUseCase = get(),
            halloweenUseCase = get(),
            userWatchlistLocalSource = get(),
            userFavoritesLocalSource = get(),
            episodeLocalDataSource = get(),
            showDetailsUpdates = get(),
            favoritesUpdates = get(),
            sessionManager = get(),
            analytics = get(),
        )
    }

    viewModel { (show: Show) ->
        ShowStreamingsViewModel(
            show = show,
            sessionManager = get(),
            getStreamingsUseCase = get(),
        )
    }

    viewModel { (show: Show) ->
        ShowExtrasViewModel(
            show = show,
            getExtrasUseCase = get(),
        )
    }

    viewModel { (show: Show) ->
        ShowSeasonsViewModel(
            show = show,
            getSeasonsUseCase = get(),
            loadUserProgressUseCase = get(),
            updateEpisodeHistoryUseCase = get(),
            showDetailsUpdates = get(),
            episodeDetailsUpdates = get(),
            sessionManager = get(),
            analytics = get(),
        )
    }

    viewModel { (show: Show) ->
        ShowActorsViewModel(
            show = show,
            getActorsUseCase = get(),
        )
    }

    viewModel { (show: Show) ->
        ShowRelatedViewModel(
            show = show,
            getRelatedShowsUseCase = get(),
            collectionStateProvider = get(),
        )
    }

    viewModel { (show: Show) ->
        ShowListsViewModel(
            show = show,
            getListsUseCase = get(),
        )
    }

    viewModel { (show: Show) ->
        ShowSentimentViewModel(
            show = show,
            getSentimentUseCase = get(),
        )
    }

    viewModel { (show: Show) ->
        ShowCommentsViewModel(
            appContext = androidApplication(),
            show = show,
            getFilterUseCase = get(),
            getCommentsUseCase = get(),
            getCommentReactionsUseCase = get(),
            loadUserReactionsUseCase = get(),
            reactionsUpdates = get(),
            sessionManager = get(),
        )
    }

    viewModel { (show: Show) ->
        ShowDetailsContextViewModel(
            show = show,
            sessionManager = get(),
            getStreamingsUseCase = get(),
        )
    }

    viewModel { (show: Show) ->
        ShowDetailsListsViewModel(
            show = show,
            sessionManager = get(),
            loadListsUseCase = get(),
        )
    }

    viewModel { (show: Show) ->
        ShowHistoryViewModel(
            show = show,
            getHistoryUseCase = get(),
            showDetailsUpdates = get(),
            episodeDetailsUpdates = get(),
        )
    }
}

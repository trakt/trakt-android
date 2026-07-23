package tv.trakt.trakt.core.summary.shows.di

import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowStorage
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.shows.ShowDetailsViewModel
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdatesStorage
import tv.trakt.trakt.core.summary.shows.features.actors.ShowActorsViewModel
import tv.trakt.trakt.core.summary.shows.features.actors.usecases.GetShowActorsUseCase
import tv.trakt.trakt.core.summary.shows.features.actors.usecases.GetShowCreatorUseCase
import tv.trakt.trakt.core.summary.shows.features.comments.ShowCommentsViewModel
import tv.trakt.trakt.core.summary.shows.features.comments.usecases.GetShowCommentsUseCase
import tv.trakt.trakt.core.summary.shows.features.context.lists.ShowDetailsListsViewModel
import tv.trakt.trakt.core.summary.shows.features.context.more.ShowDetailsContextViewModel
import tv.trakt.trakt.core.summary.shows.features.extras.ShowExtrasViewModel
import tv.trakt.trakt.core.summary.shows.features.extras.usecases.GetShowExtrasUseCase
import tv.trakt.trakt.core.summary.shows.features.history.ShowHistoryViewModel
import tv.trakt.trakt.core.summary.shows.features.history.usecases.GetShowHistoryUseCase
import tv.trakt.trakt.core.summary.shows.features.info.ShowInfoViewModel
import tv.trakt.trakt.core.summary.shows.features.info.usecase.GetShowCrewUseCase
import tv.trakt.trakt.core.summary.shows.features.info.usecase.GetShowStatsUseCase
import tv.trakt.trakt.core.summary.shows.features.info.usecase.GetShowStudiosUseCase
import tv.trakt.trakt.core.summary.shows.features.lists.ShowListsViewModel
import tv.trakt.trakt.core.summary.shows.features.lists.usecases.GetShowListsUseCase
import tv.trakt.trakt.core.summary.shows.features.related.ShowRelatedViewModel
import tv.trakt.trakt.core.summary.shows.features.related.usecases.GetShowRelatedUseCase
import tv.trakt.trakt.core.summary.shows.features.seasons.ShowSeasonsViewModel
import tv.trakt.trakt.core.summary.shows.features.seasons.all.AllShowSeasonsViewModel
import tv.trakt.trakt.core.summary.shows.features.seasons.all.usecases.GetSeasonCommentsUseCase
import tv.trakt.trakt.core.summary.shows.features.seasons.all.usecases.GetSeasonPeopleUseCase
import tv.trakt.trakt.core.summary.shows.features.seasons.usecases.GetShowSeasonsUseCase
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.WatchedUntilViewModel
import tv.trakt.trakt.core.summary.shows.features.seasons.watcheduntil.usecases.GetWatchedUntilEpisodesUseCase
import tv.trakt.trakt.core.summary.shows.features.sentiment.ShowSentimentViewModel
import tv.trakt.trakt.core.summary.shows.features.sentiment.usecases.GetShowSentimentUseCase
import tv.trakt.trakt.core.summary.shows.features.socials.GetShowSocialsUseCase
import tv.trakt.trakt.core.summary.shows.features.streaming.ShowStreamingsViewModel
import tv.trakt.trakt.core.summary.shows.features.streaming.usecases.GetShowStreamingsUseCase
import tv.trakt.trakt.core.summary.shows.features.trivia.ShowTriviaViewModel
import tv.trakt.trakt.core.summary.shows.features.trivia.usecases.GetShowTriviaUseCase
import tv.trakt.trakt.core.summary.shows.usecases.GetShowDetailsUseCase
import tv.trakt.trakt.core.summary.shows.usecases.GetShowRatingsUseCase
import tv.trakt.trakt.core.summary.shows.usecases.GetShowStreamingUseCase

internal val showDetailsDataModule = module {
    singleOf(::ShowStorage) { bind<ShowLocalDataSource>() }

    singleOf(::ShowDetailsUpdatesStorage) { bind<ShowDetailsUpdates>() }
}

internal val showDetailsModule = module {
    factoryOf(::GetShowDetailsUseCase)
    factoryOf(::GetShowRatingsUseCase)
    factoryOf(::GetShowStudiosUseCase)
    factoryOf(::GetShowStatsUseCase)
    factoryOf(::GetShowCreatorUseCase)
    factoryOf(::GetShowCrewUseCase)
    factoryOf(::GetShowSeasonsUseCase)
    factoryOf(::GetShowExtrasUseCase)
    factoryOf(::GetShowActorsUseCase)
    factoryOf(::GetSeasonPeopleUseCase)
    factoryOf(::GetSeasonCommentsUseCase)
    factoryOf(::GetShowRelatedUseCase)
    factoryOf(::GetShowStreamingsUseCase)
    factoryOf(::GetShowStreamingUseCase)
    factoryOf(::GetShowSentimentUseCase)
    factoryOf(::GetShowCommentsUseCase)
    factoryOf(::GetShowListsUseCase)
    factoryOf(::GetShowHistoryUseCase)
    factoryOf(::GetShowTriviaUseCase)
    factoryOf(::GetShowSocialsUseCase)
    factoryOf(::GetWatchedUntilEpisodesUseCase)

    viewModelOf(::ShowInfoViewModel)
    viewModelOf(::ShowStreamingsViewModel)
    viewModelOf(::ShowExtrasViewModel)
    viewModelOf(::ShowSeasonsViewModel)
    viewModel {
        AllShowSeasonsViewModel(
            savedStateHandle = get(),
            appContext = androidApplication(),
            getShowDetailsUseCase = get(),
            getSeasonsUseCase = get(),
            getSeasonsPeopleUseCase = get(),
            getSeasonCommentsUseCase = get(),
            getCommentRepliesUseCase = get(),
            getCommentReactionsUseCase = get(),
            loadUserReactionsUseCase = get(),
            loadUserRatingsUseCase = get(),
            reactionsUpdates = get(),
            ratingsUpdates = get(),
            loadUserProgressUseCase = get(),
            updateEpisodeHistoryUseCase = get(),
            episodeLocalDataSource = get(),
            showDetailsUpdates = get(),
            episodeDetailsUpdates = get(),
            sessionManager = get(),
            analytics = get(),
        )
    }
    viewModelOf(::ShowActorsViewModel)
    viewModelOf(::ShowRelatedViewModel)
    viewModelOf(::ShowListsViewModel)
    viewModelOf(::ShowSentimentViewModel)
    viewModelOf(::ShowDetailsContextViewModel)
    viewModelOf(::ShowDetailsListsViewModel)
    viewModelOf(::ShowHistoryViewModel)
    viewModelOf(::ShowTriviaViewModel)
    viewModelOf(::WatchedUntilViewModel)

    viewModel {
        ShowDetailsViewModel(
            appContext = androidApplication(),
            savedStateHandle = get(),
            getDetailsUseCase = get(),
            getExternalRatingsUseCase = get(),
            getShowCreatorUseCase = get(),
            getShowTranslationsUseCase = get(),
            getShowSocialsUseCase = get(),
            loadProgressUseCase = get(),
            loadWatchlistUseCase = get(),
            loadListsUseCase = get(),
            loadRatingUseCase = get(),
            loadFavoritesUseCase = get(),
            updateShowHistoryUseCase = get(),
            updateShowWatchlistUseCase = get(),
            updateShowFavoritesUseCase = get(),
            addListItemUseCase = get(),
            removeListItemUseCase = get(),
            appReviewUseCase = get(),
            userWatchlistLocalSource = get(),
            userWatchlistMinLocalSource = get(),
            userFavoritesLocalSource = get(),
            episodeLocalDataSource = get(),
            showDetailsUpdates = get(),
            episodeDetailsUpdates = get(),
            favoritesUpdates = get(),
            watchlistUpdates = get(),
            sessionManager = get(),
            errorsManager = get(),
            analytics = get(),
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
            commentsUpdates = get(),
            collapsingManager = get(),
        )
    }
}

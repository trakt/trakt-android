package tv.trakt.trakt.core.lists.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.core.lists.data.remote.ListsApiClient
import tv.trakt.trakt.common.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.ListsViewModel
import tv.trakt.trakt.core.lists.features.all.AllListsViewModel
import tv.trakt.trakt.core.lists.features.details.ListDetailsViewModel
import tv.trakt.trakt.core.lists.features.details.usecases.GetListItemsUseCase
import tv.trakt.trakt.core.lists.sections.collaborations.ListsCollaborationsViewModel
import tv.trakt.trakt.core.lists.sections.collaborations.data.local.items.ListsCollaborationsItemsLocalDataSource
import tv.trakt.trakt.core.lists.sections.collaborations.data.local.items.ListsCollaborationsItemsStorage
import tv.trakt.trakt.core.lists.sections.collaborations.data.local.lists.ListsCollaborationsLocalDataSource
import tv.trakt.trakt.core.lists.sections.collaborations.data.local.lists.ListsCollaborationsStorage
import tv.trakt.trakt.core.lists.sections.collaborations.usecases.GetCollaborationsListItemsUseCase
import tv.trakt.trakt.core.lists.sections.collaborations.usecases.GetCollaborationsListsUseCase
import tv.trakt.trakt.core.lists.sections.liked.ListsLikedViewModel
import tv.trakt.trakt.core.lists.sections.liked.data.local.items.ListsLikedItemsLocalDataSource
import tv.trakt.trakt.core.lists.sections.liked.data.local.items.ListsLikedItemsStorage
import tv.trakt.trakt.core.lists.sections.liked.data.local.lists.ListsLikedLocalDataSource
import tv.trakt.trakt.core.lists.sections.liked.data.local.lists.ListsLikedStorage
import tv.trakt.trakt.core.lists.sections.liked.usecases.GetLikedListItemsUseCase
import tv.trakt.trakt.core.lists.sections.liked.usecases.GetLikedListsUseCase
import tv.trakt.trakt.core.lists.sections.liked.usecases.manage.AddLikedListUseCase
import tv.trakt.trakt.core.lists.sections.liked.usecases.manage.RemoveLikedListUseCase
import tv.trakt.trakt.core.lists.sections.personal.ListsPersonalViewModel
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsStorage
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalStorage
import tv.trakt.trakt.core.lists.sections.personal.features.all.AllPersonalListViewModel
import tv.trakt.trakt.core.lists.sections.personal.features.context.movie.ListMovieContextViewModel
import tv.trakt.trakt.core.lists.sections.personal.features.context.show.ListShowContextViewModel
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListItemsUseCase
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListsUseCase
import tv.trakt.trakt.core.lists.sections.personal.usecases.manage.AddPersonalListItemUseCase
import tv.trakt.trakt.core.lists.sections.personal.usecases.manage.RemovePersonalListItemUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.ListsWatchlistViewModel
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.AllWatchlistViewModel
import tv.trakt.trakt.core.lists.sections.watchlist.features.context.movies.WatchlistMovieContextViewModel
import tv.trakt.trakt.core.lists.sections.watchlist.features.context.shows.WatchlistShowContextViewModel
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetMoviesWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetShowsWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetWatchlistUseCase
import tv.trakt.trakt.core.lists.sheets.create.CreateListViewModel
import tv.trakt.trakt.core.lists.sheets.create.usecases.CreateListUseCase
import tv.trakt.trakt.core.lists.sheets.edit.EditListViewModel
import tv.trakt.trakt.core.lists.sheets.edit.usecases.EditListUseCase
import tv.trakt.trakt.core.user.data.local.watchlist.WatchlistUpdates
import tv.trakt.trakt.core.user.data.local.watchlist.WatchlistUpdatesStorage

internal const val LISTS_PREFERENCES = "lists_preferences_mobile"

internal val listsDataModule = module {

    single<ListsRemoteDataSource> {
        ListsApiClient(
            listsApi = get(),
            cacheMarker = get(),
        )
    }

    single<DataStore<Preferences>>(named(LISTS_PREFERENCES)) {
        createStore(
            context = androidApplication(),
        )
    }

    single<ListsPersonalLocalDataSource> {
        ListsPersonalStorage()
    }

    single<ListsLikedLocalDataSource> {
        ListsLikedStorage()
    }

    single<ListsCollaborationsLocalDataSource> {
        ListsCollaborationsStorage()
    }

    single<ListsPersonalItemsLocalDataSource> {
        ListsPersonalItemsStorage()
    }

    single<ListsLikedItemsLocalDataSource> {
        ListsLikedItemsStorage()
    }

    single<ListsCollaborationsItemsLocalDataSource> {
        ListsCollaborationsItemsStorage()
    }

    single<WatchlistUpdates> {
        WatchlistUpdatesStorage()
    }
}

internal val listsModule = module {
    factory {
        GetWatchlistUseCase(
            remoteSource = get(),
            userWatchlistLocalDataSource = get(),
        )
    }

    factory {
        GetShowsWatchlistUseCase(
            remoteSource = get(),
            userWatchlistLocalDataSource = get(),
        )
    }

    factory {
        GetMoviesWatchlistUseCase(
            remoteSource = get(),
            userWatchlistLocalDataSource = get(),
        )
    }

    factory {
        GetPersonalListsUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        GetLikedListsUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        GetCollaborationsListsUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        GetPersonalListItemsUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        GetLikedListItemsUseCase(
            getListItemsUseCase = get(),
            localSource = get(),
        )
    }

    factory {
        GetCollaborationsListItemsUseCase(
            getListItemsUseCase = get(),
            localSource = get(),
        )
    }

    factory {
        GetListItemsUseCase(
            remoteSource = get(),
        )
    }

    factory {
        CreateListUseCase(
            remoteSource = get(),
        )
    }

    factory {
        EditListUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        RemovePersonalListItemUseCase(
            remoteSource = get(),
            personalListsItemsLocalDataSource = get(),
            personalListsLocalDataSource = get(),
            collabListsLocalDataSource = get(),
            collabListsItemsLocalDataSource = get(),
        )
    }

    factory {
        AddPersonalListItemUseCase(
            remoteSource = get(),
            listsItemsLocalDataSource = get(),
            listsLocalDataSource = get(),
            collabListsLocalDataSource = get(),
            collabListsItemsLocalDataSource = get(),
        )
    }

    factory {
        AddLikedListUseCase(
            remoteSource = get(),
            localSource = get(),
            listsLocalSource = get(),
        )
    }

    factory {
        RemoveLikedListUseCase(
            remoteSource = get(),
            localUserLikedListsSource = get(),
            listsLocalSource = get(),
        )
    }

    viewModel {
        ListsViewModel(
            sessionManager = get(),
            getPersonalListsUseCase = get(),
            getLikedListsUseCase = get(),
            getCollaborationsListsUseCase = get(),
            localListsSource = get(),
            localListsItemsSource = get(),
            localLikedListsSource = get(),
            localCollaborationsListsSource = get(),
            analytics = get(),
        )
    }

    viewModel {
        ListsWatchlistViewModel(
            getWatchlistUseCase = get(),
            getShowsWatchlistUseCase = get(),
            getMoviesWatchlistUseCase = get(),
            showLocalDataSource = get(),
            movieLocalDataSource = get(),
            collectionStateProvider = get(),
            modeManager = get(),
            sessionManager = get(),
            collapsingManager = get(),
            watchlistUpdates = get(),
        )
    }

    viewModel {
        AllWatchlistViewModel(
            getWatchlistUseCase = get(),
            getShowsWatchlistUseCase = get(),
            getMoviesWatchlistUseCase = get(),
            showLocalDataSource = get(),
            movieLocalDataSource = get(),
            loadUserProgressUseCase = get(),
            updateMovieHistoryUseCase = get(),
            showUpdatesSource = get(),
            episodeUpdatesSource = get(),
            movieDetailsUpdates = get(),
            watchlistUpdates = get(),
            collectionStateProvider = get(),
            modeManager = get(),
            sessionManager = get(),
            ratePromptManager = get(),
            analytics = get(),
        )
    }

    viewModel { (listId: TraktId) ->
        ListsPersonalViewModel(
            listId = listId,
            modeManager = get(),
            getListUseCase = get(),
            getListItemsUseCase = get(),
            localListsSource = get(),
            localListsItemsSource = get(),
            showLocalDataSource = get(),
            movieLocalDataSource = get(),
            episodeLocalDataSource = get(),
            collectionStateProvider = get(),
            collapsingManager = get(),
        )
    }

    viewModel { (listId: TraktId) ->
        ListsLikedViewModel(
            listId = listId,
            modeManager = get(),
            getLikedListUseCase = get(),
            getLikedListItemsUseCase = get(),
            showLocalDataSource = get(),
            movieLocalDataSource = get(),
            episodeLocalDataSource = get(),
            collectionStateProvider = get(),
            collapsingManager = get(),
        )
    }

    viewModel { (listId: TraktId) ->
        ListsCollaborationsViewModel(
            listId = listId,
            modeManager = get(),
            getCollaborationsListUseCase = get(),
            getCollaborationsListItemsUseCase = get(),
            localListsItemsSource = get(),
            localListsSource = get(),
            showLocalDataSource = get(),
            movieLocalDataSource = get(),
            episodeLocalDataSource = get(),
            collectionStateProvider = get(),
            collapsingManager = get(),
        )
    }

    viewModel {
        AllPersonalListViewModel(
            savedStateHandle = get(),
            getListUseCase = get(),
            getListItemsUseCase = get(),
            showLocalDataSource = get(),
            movieLocalDataSource = get(),
            episodeLocalDataSource = get(),
            collectionStateProvider = get(),
            sessionManager = get(),
            modeManager = get(),
            analytics = get(),
        )
    }

    viewModel {
        CreateListViewModel(
            createListUseCase = get(),
            userListsLocalDataSource = get(),
        )
    }

    viewModel {
        EditListViewModel(
            editListUseCase = get(),
            userListsLocalDataSource = get(),
        )
    }

    viewModel {
        WatchlistMovieContextViewModel(
            appContext = androidApplication(),
            updateMovieWatchlistUseCase = get(),
            userWatchlistLocalSource = get(),
            userWatchlistMinLocalSource = get(),
            updateMovieHistoryUseCase = get(),
            loadProgressUseCase = get(),
            sessionManager = get(),
            checkInManager = get(),
            ratePromptManager = get(),
            analytics = get(),
            watchlistUpdates = get(),
        )
    }

    viewModel { (show: Show) ->
        WatchlistShowContextViewModel(
            show = show,
            updateWatchlistUseCase = get(),
            updateHistoryUseCase = get(),
            userProgressLocalSource = get(),
            userWatchlistLocalSource = get(),
            userWatchlistMinLocalSource = get(),
            loadProgressUseCase = get(),
            sessionManager = get(),
            analytics = get(),
        )
    }

    viewModel { (movie: Movie, list: CustomList) ->
        ListMovieContextViewModel(
            appContext = androidApplication(),
            movie = movie,
            list = list,
            updateMovieWatchlistUseCase = get(),
            updateMovieHistoryUseCase = get(),
            removeListItemUseCase = get(),
            userProgressLocalSource = get(),
            userWatchlistLocalSource = get(),
            userWatchlistMinLocalSource = get(),
            loadProgressUseCase = get(),
            loadWatchlistMinUseCase = get(),
            watchlistUpdates = get(),
            sessionManager = get(),
            checkInManager = get(),
            ratePromptManager = get(),
            errorsManager = get(),
            analytics = get(),
        )
    }

    viewModel { (show: Show, list: CustomList) ->
        ListShowContextViewModel(
            show = show,
            list = list,
            updateShowWatchlistUseCase = get(),
            updateShowHistoryUseCase = get(),
            removeListItemUseCase = get(),
            userProgressLocalSource = get(),
            userWatchlistLocalSource = get(),
            userWatchlistMinLocalSource = get(),
            loadProgressUseCase = get(),
            loadWatchlistMinUseCase = get(),
            watchlistUpdates = get(),
            sessionManager = get(),
            errorsManager = get(),
            analytics = get(),
        )
    }

    viewModel {
        ListDetailsViewModel(
            savedStateHandle = get(),
            getListItemsUseCase = get(),
            getListLikedUseCase = get(),
            addLikedListUseCase = get(),
            removeLikedListUseCase = get(),
            showLocalDataSource = get(),
            movieLocalDataSource = get(),
            episodeLocalDataSource = get(),
            collectionStateProvider = get(),
            sessionManager = get(),
        )
    }

    viewModel {
        AllListsViewModel(
            savedStateHandle = get(),
            sessionManager = get(),
            analytics = get(),
            getPersonalListsUseCase = get(),
            getLikedListsUseCase = get(),
            getCollaborationsListsUseCase = get(),
            localPersonalListsSource = get(),
            localLikedListsSource = get(),
            localCollaborationsListsSource = get(),
        )
    }
}

private fun createStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { emptyPreferences() },
        ),
        migrations = listOf(SharedPreferencesMigration(context, LISTS_PREFERENCES)),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(LISTS_PREFERENCES) },
    )
}

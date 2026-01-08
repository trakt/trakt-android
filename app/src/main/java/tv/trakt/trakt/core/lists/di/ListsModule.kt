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
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.ListsViewModel
import tv.trakt.trakt.core.lists.data.remote.ListsApiClient
import tv.trakt.trakt.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.core.lists.features.details.ListDetailsViewModel
import tv.trakt.trakt.core.lists.features.details.usecases.GetListItemsUseCase
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
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.data.AllWatchlistLocalDataSource
import tv.trakt.trakt.core.lists.sections.watchlist.features.all.data.AllWatchlistStorage
import tv.trakt.trakt.core.lists.sections.watchlist.features.context.movies.WatchlistMovieContextViewModel
import tv.trakt.trakt.core.lists.sections.watchlist.features.context.shows.WatchlistShowContextViewModel
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetMoviesWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetShowsWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetWatchlistUseCase
import tv.trakt.trakt.core.lists.sheets.create.CreateListViewModel
import tv.trakt.trakt.core.lists.sheets.create.usecases.CreateListUseCase
import tv.trakt.trakt.core.lists.sheets.edit.EditListViewModel
import tv.trakt.trakt.core.lists.sheets.edit.usecases.EditListUseCase

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

    single<AllWatchlistLocalDataSource> {
        AllWatchlistStorage()
    }

    single<ListsPersonalLocalDataSource> {
        ListsPersonalStorage()
    }

    single<ListsPersonalItemsLocalDataSource> {
        ListsPersonalItemsStorage()
    }
}

internal val listsModule = module {
    factory {
        GetWatchlistUseCase(
            loadUserWatchlistUseCase = get(),
        )
    }

    factory {
        GetShowsWatchlistUseCase(
            loadUserWatchlistUseCase = get(),
        )
    }

    factory {
        GetMoviesWatchlistUseCase(
            loadUserWatchlistUseCase = get(),
        )
    }

//    factory {
//        GetWatchlistFilterUseCase(
//            dataStore = get(named(LISTS_PREFERENCES)),
//        )
//    }

    factory {
        GetPersonalListsUseCase(
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
            listsItemsLocalDataSource = get(),
            listsLocalDataSource = get(),
            userListsLocalDataSource = get(),
        )
    }

    factory {
        AddPersonalListItemUseCase(
            remoteSource = get(),
            listsItemsLocalDataSource = get(),
            userListsLocalDataSource = get(),
            listsLocalDataSource = get(),
        )
    }

    viewModel {
        ListsViewModel(
            sessionManager = get(),
            getPersonalListsUseCase = get(),
            localListsSource = get(),
            localListsItemsSource = get(),
            analytics = get(),
        )
    }

    viewModel {
        ListsWatchlistViewModel(
            getWatchlistUseCase = get(),
            getShowsWatchlistUseCase = get(),
            getMoviesWatchlistUseCase = get(),
            userWatchlistSource = get(),
            allWatchlistSource = get(),
            showLocalDataSource = get(),
            movieLocalDataSource = get(),
            collectionStateProvider = get(),
            modeManager = get(),
            sessionManager = get(),
            collapsingManager = get(),
        )
    }

    viewModel {
        AllWatchlistViewModel(
            getWatchlistUseCase = get(),
            getShowsWatchlistUseCase = get(),
            getMoviesWatchlistUseCase = get(),
            allWatchlistLocalDataSource = get(),
            showLocalDataSource = get(),
            movieLocalDataSource = get(),
            loadUserProgressUseCase = get(),
            updateMovieHistoryUseCase = get(),
            showUpdatesSource = get(),
            episodeUpdatesSource = get(),
            movieDetailsUpdates = get(),
            collectionStateProvider = get(),
            modeManager = get(),
            sessionManager = get(),
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
            userListLocalDataSource = get(),
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
            updateMovieWatchlistUseCase = get(),
            userWatchlistLocalSource = get(),
            updateMovieHistoryUseCase = get(),
            loadProgressUseCase = get(),
            sessionManager = get(),
            analytics = get(),
        )
    }

    viewModel { (show: Show) ->
        WatchlistShowContextViewModel(
            show = show,
            updateWatchlistUseCase = get(),
            updateHistoryUseCase = get(),
            userProgressLocalSource = get(),
            userWatchlistLocalSource = get(),
            loadProgressUseCase = get(),
            sessionManager = get(),
            analytics = get(),
        )
    }

    viewModel { (movie: Movie, list: CustomList) ->
        ListMovieContextViewModel(
            movie = movie,
            list = list,
            updateMovieWatchlistUseCase = get(),
            updateMovieHistoryUseCase = get(),
            removeListItemUseCase = get(),
            userProgressLocalSource = get(),
            userWatchlistLocalSource = get(),
            loadProgressUseCase = get(),
            loadWatchlistUseCase = get(),
            sessionManager = get(),
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
            loadProgressUseCase = get(),
            loadWatchlistUseCase = get(),
            sessionManager = get(),
            analytics = get(),
        )
    }

    viewModel {
        ListDetailsViewModel(
            savedStateHandle = get(),
            getListItemsUseCase = get(),
            showLocalDataSource = get(),
            movieLocalDataSource = get(),
            sessionManager = get(),
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

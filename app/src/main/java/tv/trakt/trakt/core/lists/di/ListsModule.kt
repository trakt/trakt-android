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
import org.koin.android.ext.koin.androidContext
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
import tv.trakt.trakt.core.lists.sections.personal.ListsPersonalViewModel
import tv.trakt.trakt.core.lists.sections.personal.context.movie.ListMovieContextViewModel
import tv.trakt.trakt.core.lists.sections.personal.context.show.ListShowContextViewModel
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsStorage
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalStorage
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListItemsUseCase
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListsUseCase
import tv.trakt.trakt.core.lists.sections.personal.usecases.RemovePersonalListItemUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.ListsWatchlistViewModel
import tv.trakt.trakt.core.lists.sections.watchlist.all.AllWatchlistViewModel
import tv.trakt.trakt.core.lists.sections.watchlist.context.movies.WatchlistMovieContextViewModel
import tv.trakt.trakt.core.lists.sections.watchlist.context.shows.WatchlistShowContextViewModel
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetMoviesWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetShowsWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.filters.GetWatchlistFilterUseCase
import tv.trakt.trakt.core.lists.sheets.create.CreateListViewModel
import tv.trakt.trakt.core.lists.sheets.create.usecases.CreateListUseCase
import tv.trakt.trakt.core.lists.sheets.edit.EditListViewModel
import tv.trakt.trakt.core.lists.sheets.edit.usecases.EditListUseCase

internal const val LISTS_PREFERENCES = "lists_preferences_mobile"

internal val listsDataModule = module {

    single<ListsRemoteDataSource> {
        ListsApiClient(
            listsApi = get(),
        )
    }

    single<DataStore<Preferences>>(named(LISTS_PREFERENCES)) {
        createStore(
            context = androidContext(),
        )
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

    factory {
        GetWatchlistFilterUseCase(
            dataStore = get(named(LISTS_PREFERENCES)),
        )
    }

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
            localSource = get(),
        )
    }

    viewModel {
        ListsViewModel(
            sessionManager = get(),
            getPersonalListsUseCase = get(),
        )
    }

    viewModel {
        ListsWatchlistViewModel(
            getWatchlistUseCase = get(),
            getShowsWatchlistUseCase = get(),
            getMoviesWatchlistUseCase = get(),
            getFilterUseCase = get(),
            userWatchlistSource = get(),
            sessionManager = get(),
        )
    }

    viewModel {
        AllWatchlistViewModel(
            getWatchlistUseCase = get(),
            getShowsWatchlistUseCase = get(),
            getMoviesWatchlistUseCase = get(),
            getFilterUseCase = get(),
            sessionManager = get(),
        )
    }

    viewModel { (listId: TraktId) ->
        ListsPersonalViewModel(
            listId = listId,
            getListItemsUseCase = get(),
        )
    }

    viewModel {
        CreateListViewModel(
            createListUseCase = get(),
        )
    }

    viewModel {
        EditListViewModel(
            editListUseCase = get(),
        )
    }

    viewModel {
        WatchlistMovieContextViewModel(
            updateMovieWatchlistUseCase = get(),
            userWatchlistLocalSource = get(),
            updateMovieHistoryUseCase = get(),
            loadProgressUseCase = get(),
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

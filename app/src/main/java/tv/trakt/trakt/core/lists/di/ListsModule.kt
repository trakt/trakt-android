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
import org.openapitools.client.apis.ListsApi
import tv.trakt.trakt.common.Config.API_HD_BASE_URL
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.ListsViewModel
import tv.trakt.trakt.core.lists.data.remote.ListsApiClient
import tv.trakt.trakt.core.lists.data.remote.ListsRemoteDataSource
import tv.trakt.trakt.core.lists.sections.create.CreateListViewModel
import tv.trakt.trakt.core.lists.sections.create.usecases.CreateListUseCase
import tv.trakt.trakt.core.lists.sections.personal.ListsPersonalViewModel
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsStorage
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalStorage
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListItemsUseCase
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListsUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.ListsWatchlistViewModel
import tv.trakt.trakt.core.lists.sections.watchlist.data.local.ListsWatchlistLocalDataSource
import tv.trakt.trakt.core.lists.sections.watchlist.data.local.ListsWatchlistStorage
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetMoviesWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetShowsWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.filters.GetWatchlistFilterUseCase

internal const val LISTS_PREFERENCES = "lists_preferences_mobile"

internal const val LISTS_WATCHLIST_STORAGE = "lists_watchlist_storage"
internal const val LISTS_SHOWS_WATCHLIST_STORAGE = "lists_shows_watchlist_storage"
internal const val LISTS_MOVIES_WATCHLIST_STORAGE = "lists_movies_watchlist_storage"

internal val listsDataModule = module {

    single<ListsRemoteDataSource> {
        ListsApiClient(
            listsApi = ListsApi(
                baseUrl = API_HD_BASE_URL,
                httpClientEngine = get(),
                httpClientConfig = get(named("authorizedClientConfig")),
            ),
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

    arrayOf(
        LISTS_WATCHLIST_STORAGE,
        LISTS_SHOWS_WATCHLIST_STORAGE,
        LISTS_MOVIES_WATCHLIST_STORAGE,
    ).forEach {
        single<ListsWatchlistLocalDataSource>(named(it)) {
            ListsWatchlistStorage()
        }
    }
}

@Suppress("UndeclaredKoinUsage")
internal val listsModule = module {
    factory {
        GetWatchlistUseCase(
            remoteSource = get(),
            localSource = get(named(LISTS_WATCHLIST_STORAGE)),
        )
    }

    factory {
        GetShowsWatchlistUseCase(
            remoteSyncSource = get(),
            localSource = get(named(LISTS_SHOWS_WATCHLIST_STORAGE)),
        )
    }

    factory {
        GetMoviesWatchlistUseCase(
            remoteSyncSource = get(),
            localSource = get(named(LISTS_MOVIES_WATCHLIST_STORAGE)),
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

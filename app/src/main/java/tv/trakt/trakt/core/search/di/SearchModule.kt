package tv.trakt.trakt.core.search.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.SearchApi
import tv.trakt.trakt.common.Config.API_HD_BASE_URL
import tv.trakt.trakt.core.search.SearchViewModel
import tv.trakt.trakt.core.search.data.local.RecentSearchLocalDataSource
import tv.trakt.trakt.core.search.data.local.RecentSearchStorage
import tv.trakt.trakt.core.search.data.local.people.SearchPeopleLocalDataSource
import tv.trakt.trakt.core.search.data.local.people.SearchPeopleStorage
import tv.trakt.trakt.core.search.data.remote.SearchApiClient
import tv.trakt.trakt.core.search.data.remote.SearchRemoteDataSource
import tv.trakt.trakt.core.search.usecase.GetBirthdayPeopleUseCase
import tv.trakt.trakt.core.search.usecase.GetSearchResultsUseCase
import tv.trakt.trakt.core.search.usecase.recents.AddRecentSearchUseCase
import tv.trakt.trakt.core.search.usecase.recents.GetRecentSearchUseCase

private const val SEARCH_PREFERENCES = "search_preferences"

internal val searchDataModule = module {
    single<SearchRemoteDataSource> {
        SearchApiClient(
            api = SearchApi(
                baseUrl = API_HD_BASE_URL,
                httpClientEngine = get(),
                httpClientConfig = get(named("clientConfig")),
            ),
        )
    }

    single<SearchPeopleLocalDataSource> {
        SearchPeopleStorage()
    }

    single<RecentSearchLocalDataSource> {
        RecentSearchStorage(
            dataStore = get(named(SEARCH_PREFERENCES)),
        )
    }

    single<DataStore<Preferences>>(named(SEARCH_PREFERENCES)) {
        createStore(
            context = androidContext(),
        )
    }
}

internal val searchModule = module {

    factory {
        GetSearchResultsUseCase(
            remoteSource = get(),
        )
    }

    factory {
        AddRecentSearchUseCase(
            sessionManager = get(),
            recentsLocalSource = get(),
        )
    }

    factory {
        GetRecentSearchUseCase(
            recentsLocalSource = get(),
        )
    }

    factory {
        GetBirthdayPeopleUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    viewModel { (_: SavedStateHandle) ->
        SearchViewModel(
            getSearchResultsUseCase = get(),
            addRecentSearchUseCase = get(),
            getRecentSearchUseCase = get(),
            getTrendingShowsUseCase = get(),
            getTrendingMoviesUseCase = get(),
            getBirthdayPeopleUseCase = get(),
            sessionManager = get(),
        )
    }
}

private fun createStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { emptyPreferences() },
        ),
        migrations = listOf(SharedPreferencesMigration(context, SEARCH_PREFERENCES)),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(SEARCH_PREFERENCES) },
    )
}

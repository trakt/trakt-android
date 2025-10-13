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
import tv.trakt.trakt.core.search.SearchViewModel
import tv.trakt.trakt.core.search.data.local.RecentSearchLocalDataSource
import tv.trakt.trakt.core.search.data.local.RecentSearchStorage
import tv.trakt.trakt.core.search.data.local.people.SearchPeopleLocalDataSource
import tv.trakt.trakt.core.search.data.local.people.SearchPeopleStorage
import tv.trakt.trakt.core.search.data.local.popular.PopularSearchLocalDataSource
import tv.trakt.trakt.core.search.data.local.popular.PopularSearchStorage
import tv.trakt.trakt.core.search.data.remote.SearchApiClient
import tv.trakt.trakt.core.search.data.remote.SearchRemoteDataSource
import tv.trakt.trakt.core.search.usecase.GetBirthdayPeopleUseCase
import tv.trakt.trakt.core.search.usecase.GetSearchResultsUseCase
import tv.trakt.trakt.core.search.usecase.popular.GetPopularSearchUseCase
import tv.trakt.trakt.core.search.usecase.popular.PostUserSearchUseCase
import tv.trakt.trakt.core.search.usecase.recents.AddRecentSearchUseCase
import tv.trakt.trakt.core.search.usecase.recents.GetRecentSearchUseCase

private const val SEARCH_PREFERENCES = "search_preferences"

internal val searchDataModule = module {
    single<SearchRemoteDataSource> {
        SearchApiClient(
            api = get(),
            authorizedApi = get(named("authorizedSearchApi")),
        )
    }

    single<SearchPeopleLocalDataSource> {
        SearchPeopleStorage(
            dataStore = get(named(SEARCH_PREFERENCES)),
        )
    }

    single<RecentSearchLocalDataSource> {
        RecentSearchStorage(
            dataStore = get(named(SEARCH_PREFERENCES)),
        )
    }

    single<PopularSearchLocalDataSource> {
        PopularSearchStorage(
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
        GetPopularSearchUseCase(
            remoteSource = get(),
            localSource = get(),
        )
    }

    factory {
        PostUserSearchUseCase(
            remoteSource = get(),
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
            postUserSearchUseCase = get(),
            getPopularSearchesUseCase = get(),
            getSearchResultsUseCase = get(),
            addRecentSearchUseCase = get(),
            getRecentSearchUseCase = get(),
            getBirthdayPeopleUseCase = get(),
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
        migrations = listOf(SharedPreferencesMigration(context, SEARCH_PREFERENCES)),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(SEARCH_PREFERENCES) },
    )
}

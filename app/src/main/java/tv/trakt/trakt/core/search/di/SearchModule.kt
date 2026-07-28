package tv.trakt.trakt.core.search.di

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
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.core.search.data.remote.SearchApiClient
import tv.trakt.trakt.common.core.search.data.remote.SearchRemoteDataSource
import tv.trakt.trakt.common.core.search.usecase.GetSearchResultsUseCase
import tv.trakt.trakt.core.search.SearchViewModel
import tv.trakt.trakt.core.search.data.local.people.SearchPeopleLocalDataSource
import tv.trakt.trakt.core.search.data.local.people.SearchPeopleStorage
import tv.trakt.trakt.core.search.data.local.popular.PopularSearchLocalDataSource
import tv.trakt.trakt.core.search.data.local.popular.PopularSearchStorage
import tv.trakt.trakt.core.search.usecase.GetBirthdayPeopleUseCase
import tv.trakt.trakt.core.search.usecase.popular.GetPopularSearchUseCase
import tv.trakt.trakt.core.search.usecase.popular.PostUserSearchUseCase

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

    single<PopularSearchLocalDataSource> {
        PopularSearchStorage(
            dataStore = get(named(SEARCH_PREFERENCES)),
        )
    }

    single<DataStore<Preferences>>(named(SEARCH_PREFERENCES)) {
        createStore(
            context = androidApplication(),
        )
    }
}

internal val searchModule = module {
    factoryOf(::GetSearchResultsUseCase)
    factoryOf(::GetPopularSearchUseCase)
    factoryOf(::PostUserSearchUseCase)
    factoryOf(::GetBirthdayPeopleUseCase)

    viewModelOf(::SearchViewModel)
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

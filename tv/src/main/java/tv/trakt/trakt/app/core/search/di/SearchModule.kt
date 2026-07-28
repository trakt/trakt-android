package tv.trakt.trakt.app.core.search.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.app.core.main.di.createStore
import tv.trakt.trakt.app.core.search.SearchViewModel
import tv.trakt.trakt.app.core.search.data.local.RecentSearchLocalDataSource
import tv.trakt.trakt.app.core.search.data.local.RecentSearchStorage
import tv.trakt.trakt.app.core.search.usecase.recents.AddRecentSearchUseCase
import tv.trakt.trakt.app.core.search.usecase.recents.GetRecentSearchUseCase
import tv.trakt.trakt.common.core.search.data.remote.SearchApiClient
import tv.trakt.trakt.common.core.search.data.remote.SearchRemoteDataSource
import tv.trakt.trakt.common.core.search.usecase.GetSearchResultsUseCase

private const val SEARCH_PREFERENCES = "search_preferences_tv_v2"

internal val searchDataModule = module {
    single<SearchRemoteDataSource> {
        SearchApiClient(
            api = get(),
            authorizedApi = get(named("authorizedSearchApi")),
        )
    }

    single<RecentSearchLocalDataSource> {
        RecentSearchStorage(
            dataStore = get(named(SEARCH_PREFERENCES)),
        )
    }

    single<DataStore<Preferences>>(named(SEARCH_PREFERENCES)) {
        createStore(
            context = androidContext(),
            key = SEARCH_PREFERENCES,
        )
    }
}

internal val searchModule = module {
    factoryOf(::GetSearchResultsUseCase)
    factoryOf(::GetRecentSearchUseCase)
    factoryOf(::AddRecentSearchUseCase)

    viewModelOf(::SearchViewModel)
}

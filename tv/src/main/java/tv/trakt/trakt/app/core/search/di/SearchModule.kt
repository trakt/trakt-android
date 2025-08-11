package tv.trakt.trakt.app.core.search.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.SavedStateHandle
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.SearchApi
import tv.trakt.trakt.app.Config.API_BASE_URL
import tv.trakt.trakt.app.core.main.di.createStore
import tv.trakt.trakt.app.core.search.SearchViewModel
import tv.trakt.trakt.app.core.search.data.local.RecentSearchLocalDataSource
import tv.trakt.trakt.app.core.search.data.local.RecentSearchStorage
import tv.trakt.trakt.app.core.search.data.remote.SearchApiClient
import tv.trakt.trakt.app.core.search.data.remote.SearchRemoteDataSource
import tv.trakt.trakt.app.core.search.usecase.GetSearchResultsUseCase
import tv.trakt.trakt.app.core.search.usecase.recents.AddRecentSearchUseCase
import tv.trakt.trakt.app.core.search.usecase.recents.GetRecentSearchUseCase

private const val SEARCH_PREFERENCES = "search_preferences"

internal val searchDataModule = module {
    single<SearchRemoteDataSource> {
        val httpClientEngine = get<HttpClientEngine>()
        val httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("clientConfig"))

        SearchApiClient(
            api = SearchApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = httpClientEngine,
                httpClientConfig = httpClientConfig,
            ),
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

    factory {
        GetSearchResultsUseCase(
            remoteSource = get(),
        )
    }

    factory {
        AddRecentSearchUseCase(
            recentsLocalSource = get(),
        )
    }

    factory {
        GetRecentSearchUseCase(
            recentsLocalSource = get(),
        )
    }

    viewModel { (_: SavedStateHandle) ->
        SearchViewModel(
            getSearchResultsUseCase = get(),
            addRecentSearchUseCase = get(),
            getRecentSearchUseCase = get(),
            getTrendingShowsUseCase = get(),
            getTrendingMoviesUseCase = get(),
            showLocalSource = get(),
            movieLocalSource = get(),
        )
    }
}

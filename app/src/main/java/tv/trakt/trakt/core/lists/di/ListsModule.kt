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
import tv.trakt.trakt.core.lists.ListsViewModel
import tv.trakt.trakt.core.lists.sections.watchlist.ListsWatchlistViewModel
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetMoviesWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetShowsWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.GetWatchlistUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.usecases.filters.GetWatchlistFilterUseCase

internal const val LISTS_PREFERENCES = "lists_preferences_mobile"

internal val listsDataModule = module {
    single<DataStore<Preferences>>(named(LISTS_PREFERENCES)) {
        createStore(
            context = androidContext(),
        )
    }

//
//    single<ShowsRemoteDataSource> {
//        ShowsApiClient(
//            showsApi = ShowsApi(
//                baseUrl = API_BASE_URL,
//                httpClientEngine = get(),
//                httpClientConfig = get(named("clientConfig")),
//            ),
//            recommendationsApi = RecommendationsApi(
//                baseUrl = API_BASE_URL,
//                httpClientEngine = get(),
//                httpClientConfig = get(named("authorizedClientConfig")),
//            ),
//        )
//    }
//
//    single<TrendingShowsLocalDataSource> {
//        TrendingShowsStorage()
//    }
//
//    single<RecommendedShowsLocalDataSource> {
//        RecommendedShowsStorage()
//    }
//
//    single<PopularShowsLocalDataSource> {
//        PopularShowsStorage()
//    }
//
//    single<AnticipatedShowsLocalDataSource> {
//        AnticipatedShowsStorage()
//    }
}

internal val listsModule = module {
    factory {
        GetWatchlistUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetShowsWatchlistUseCase(
            remoteSyncSource = get(),
        )
    }

    factory {
        GetMoviesWatchlistUseCase(
            remoteSyncSource = get(),
        )
    }

    factory {
        GetWatchlistFilterUseCase(
            dataStore = get(named(LISTS_PREFERENCES)),
        )
    }

    viewModel {
        ListsViewModel(
            sessionManager = get(),
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

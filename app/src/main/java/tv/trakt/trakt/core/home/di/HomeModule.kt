package tv.trakt.trakt.core.home.di

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
import tv.trakt.trakt.core.home.HomeViewModel
import tv.trakt.trakt.core.home.sections.activity.HomeActivityViewModel
import tv.trakt.trakt.core.home.sections.activity.usecases.GetActivityFilterUseCase
import tv.trakt.trakt.core.home.sections.activity.usecases.GetPersonalActivityUseCase
import tv.trakt.trakt.core.home.sections.activity.usecases.GetSocialActivityUseCase
import tv.trakt.trakt.core.home.sections.upnext.HomeUpNextViewModel
import tv.trakt.trakt.core.home.sections.upnext.data.local.HomeUpNextLocalDataSource
import tv.trakt.trakt.core.home.sections.upnext.data.local.HomeUpNextStorage
import tv.trakt.trakt.core.home.sections.upnext.usecases.GetUpNextUseCase
import tv.trakt.trakt.core.home.sections.watchlist.HomeWatchlistViewModel
import tv.trakt.trakt.core.home.sections.watchlist.data.local.HomeWatchlistLocalDataSource
import tv.trakt.trakt.core.home.sections.watchlist.data.local.HomeWatchlistStorage
import tv.trakt.trakt.core.home.sections.watchlist.usecases.GetWatchlistMoviesUseCase

internal const val HOME_PREFERENCES = "home_preferences_mobile"

internal val homeDataModule = module {
    single<DataStore<Preferences>>(named(HOME_PREFERENCES)) {
        createStore(
            context = androidContext(),
        )
    }

    single<HomeUpNextLocalDataSource> {
        HomeUpNextStorage()
    }

    single<HomeWatchlistLocalDataSource> {
        HomeWatchlistStorage()
    }
}

internal val homeModule = module {

    factory {
        GetUpNextUseCase(
            remoteSyncSource = get(),
            localDataSource = get(),
        )
    }

    factory {
        GetWatchlistMoviesUseCase(
            remoteSyncSource = get(),
            localDataSource = get(),
        )
    }

    factory {
        GetSocialActivityUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetPersonalActivityUseCase(
            remoteUserSource = get(),
        )
    }

    factory {
        GetActivityFilterUseCase(
            homeDataStore = get(named(HOME_PREFERENCES)),
        )
    }

    viewModel {
        HomeViewModel(
            sessionManager = get(),
        )
    }

    viewModel {
        HomeUpNextViewModel(
            getUpNextUseCase = get(),
        )
    }

    viewModel {
        HomeWatchlistViewModel(
            getWatchlistUseCase = get(),
        )
    }

    viewModel {
        HomeActivityViewModel(
            getSocialActivityUseCase = get(),
            getPersonalActivityUseCase = get(),
            getActivityFilterUseCase = get(),
        )
    }
}

private fun createStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { emptyPreferences() },
        ),
        migrations = listOf(SharedPreferencesMigration(context, HOME_PREFERENCES)),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(HOME_PREFERENCES) },
    )
}

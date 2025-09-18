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
import tv.trakt.trakt.core.home.sections.activity.data.local.personal.HomePersonalLocalDataSource
import tv.trakt.trakt.core.home.sections.activity.data.local.personal.HomePersonalStorage
import tv.trakt.trakt.core.home.sections.activity.data.local.social.HomeSocialLocalDataSource
import tv.trakt.trakt.core.home.sections.activity.data.local.social.HomeSocialStorage
import tv.trakt.trakt.core.home.sections.activity.usecases.GetActivityFilterUseCase
import tv.trakt.trakt.core.home.sections.activity.usecases.GetPersonalActivityUseCase
import tv.trakt.trakt.core.home.sections.activity.usecases.GetSocialActivityUseCase
import tv.trakt.trakt.core.home.sections.activity.views.context.ActivityItemContextViewModel
import tv.trakt.trakt.core.home.sections.upcoming.HomeUpcomingViewModel
import tv.trakt.trakt.core.home.sections.upcoming.data.local.HomeUpcomingLocalDataSource
import tv.trakt.trakt.core.home.sections.upcoming.data.local.HomeUpcomingStorage
import tv.trakt.trakt.core.home.sections.upcoming.usecases.GetUpcomingUseCase
import tv.trakt.trakt.core.home.sections.upnext.HomeUpNextViewModel
import tv.trakt.trakt.core.home.sections.upnext.data.local.HomeUpNextLocalDataSource
import tv.trakt.trakt.core.home.sections.upnext.data.local.HomeUpNextStorage
import tv.trakt.trakt.core.home.sections.upnext.usecases.GetUpNextUseCase
import tv.trakt.trakt.core.home.sections.upnext.views.UpNextItemContextViewModel
import tv.trakt.trakt.core.home.sections.watchlist.HomeWatchlistViewModel
import tv.trakt.trakt.core.home.sections.watchlist.usecases.AddWatchlistHistoryUseCase
import tv.trakt.trakt.core.home.sections.watchlist.usecases.GetWatchlistMoviesUseCase
import tv.trakt.trakt.core.home.sections.watchlist.views.WatchlistItemContextViewModel

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

    single<HomeSocialLocalDataSource> {
        HomeSocialStorage()
    }

    single<HomePersonalLocalDataSource> {
        HomePersonalStorage()
    }

    single<HomeUpcomingLocalDataSource> {
        HomeUpcomingStorage()
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
            loadUserWatchlistUseCase = get(),
        )
    }

    factory {
        AddWatchlistHistoryUseCase(
            updateHistoryUseCase = get(),
            userWatchlistLocalSource = get(),
        )
    }

    factory {
        GetSocialActivityUseCase(
            remoteSource = get(),
            localDataSource = get(),
        )
    }

    factory {
        GetPersonalActivityUseCase(
            remoteUserSource = get(),
            localDataSource = get(),
        )
    }

    factory {
        GetActivityFilterUseCase(
            homeDataStore = get(named(HOME_PREFERENCES)),
        )
    }

    factory {
        GetUpcomingUseCase(
            remoteUserSource = get(),
            localDataSource = get(),
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
            updateHistoryUseCase = get(),
            homePersonalActivitySource = get(),
            sessionManager = get(),
        )
    }

    viewModel {
        HomeWatchlistViewModel(
            getWatchlistUseCase = get(),
            addHistoryUseCase = get(),
            sessionManager = get(),
        )
    }

    viewModel {
        HomeActivityViewModel(
            getSocialActivityUseCase = get(),
            getPersonalActivityUseCase = get(),
            getActivityFilterUseCase = get(),
            homeUpNextSource = get(),
            userWatchlistSource = get(),
            sessionManager = get(),
        )
    }

    viewModel {
        HomeUpcomingViewModel(
            getUpcomingUseCase = get(),
            sessionManager = get(),
        )
    }

    viewModel {
        ActivityItemContextViewModel(
            updateMovieHistoryUseCase = get(),
            activityLocalSource = get(),
        )
    }

    viewModel {
        UpNextItemContextViewModel(
            updateShowHistoryUseCase = get(),
            upNextLocalDataSource = get(),
        )
    }

    viewModel {
        WatchlistItemContextViewModel(
            updateMovieWatchlistUseCase = get(),
            userWatchlistLocalSource = get(),
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

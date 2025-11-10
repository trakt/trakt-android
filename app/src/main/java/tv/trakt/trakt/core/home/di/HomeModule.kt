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
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.core.home.HomeViewModel
import tv.trakt.trakt.core.home.sections.activity.data.local.personal.HomePersonalLocalDataSource
import tv.trakt.trakt.core.home.sections.activity.data.local.personal.HomePersonalStorage
import tv.trakt.trakt.core.home.sections.activity.data.local.social.HomeSocialLocalDataSource
import tv.trakt.trakt.core.home.sections.activity.data.local.social.HomeSocialStorage
import tv.trakt.trakt.core.home.sections.activity.features.all.data.local.AllActivityLocalDataSource
import tv.trakt.trakt.core.home.sections.activity.features.all.data.local.AllActivityStorage
import tv.trakt.trakt.core.home.sections.activity.features.all.personal.AllActivityPersonalViewModel
import tv.trakt.trakt.core.home.sections.activity.features.all.social.AllActivitySocialViewModel
import tv.trakt.trakt.core.home.sections.activity.features.history.HomeHistoryViewModel
import tv.trakt.trakt.core.home.sections.activity.features.social.HomeSocialViewModel
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
import tv.trakt.trakt.core.home.sections.upnext.features.all.AllHomeUpNextViewModel
import tv.trakt.trakt.core.home.sections.upnext.features.all.data.local.AllUpNextLocalDataSource
import tv.trakt.trakt.core.home.sections.upnext.features.all.data.local.AllUpNextStorage
import tv.trakt.trakt.core.home.sections.upnext.features.context.UpNextItemContextViewModel
import tv.trakt.trakt.core.home.sections.upnext.usecases.GetUpNextUseCase
import tv.trakt.trakt.core.home.sections.watchlist.HomeWatchlistViewModel
import tv.trakt.trakt.core.home.sections.watchlist.usecases.AddHomeHistoryUseCase
import tv.trakt.trakt.core.home.sections.watchlist.usecases.GetHomeWatchlistUseCase

internal const val HOME_PREFERENCES = "home_preferences_mobile"

internal val homeDataModule = module {
    single<DataStore<Preferences>>(named(HOME_PREFERENCES)) {
        createStore(
            context = androidApplication(),
        )
    }

    single<HomeUpNextLocalDataSource> {
        HomeUpNextStorage()
    }

    single<AllUpNextLocalDataSource> {
        AllUpNextStorage()
    }

    single<HomeSocialLocalDataSource> {
        HomeSocialStorage()
    }

    single<HomePersonalLocalDataSource> {
        HomePersonalStorage()
    }

    single<AllActivityLocalDataSource> {
        AllActivityStorage()
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
            localShowSource = get(),
            localEpisodeSource = get(),
        )
    }

    factory {
        GetHomeWatchlistUseCase(
            loadUserWatchlistUseCase = get(),
        )
    }

    factory {
        AddHomeHistoryUseCase(
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
        GetUpcomingUseCase(
            remoteUserSource = get(),
            localDataSource = get(),
        )
    }

    viewModel {
        HomeViewModel(
            sessionManager = get(),
            analytics = get(),
        )
    }

    viewModel {
        HomeUpNextViewModel(
            getUpNextUseCase = get(),
            updateHistoryUseCase = get(),
            homePersonalActivitySource = get(),
            allUpNextSource = get(),
            loadUserProgressUseCase = get(),
            showUpdatesSource = get(),
            episodeUpdatesSource = get(),
            sessionManager = get(),
            analytics = get(),
        )
    }

    viewModel {
        AllHomeUpNextViewModel(
            getUpNextUseCase = get(),
            updateHistoryUseCase = get(),
            loadUserProgressUseCase = get(),
            allUpNextSource = get(),
            showUpdatesSource = get(),
            episodeUpdatesSource = get(),
            movieDetailsUpdates = get(),
            sessionManager = get(),
            analytics = get(),
        )
    }

    viewModel {
        HomeWatchlistViewModel(
            getWatchlistUseCase = get(),
            addHistoryUseCase = get(),
            loadUserProgressUseCase = get(),
            allWatchlistSource = get(),
            userWatchlistSource = get(),
            movieLocalDataSource = get(),
            sessionManager = get(),
            analytics = get(),
        )
    }

    viewModel {
        HomeSocialViewModel(
            getSocialActivityUseCase = get(),
            showLocalDataSource = get(),
            episodeLocalDataSource = get(),
            movieLocalDataSource = get(),
            sessionManager = get(),
        )
    }

    viewModel {
        HomeHistoryViewModel(
            getPersonalActivityUseCase = get(),
            homeUpNextSource = get(),
            userWatchlistSource = get(),
            allActivitySource = get(),
            showLocalDataSource = get(),
            showUpdatesSource = get(),
            episodeUpdatesSource = get(),
            episodeLocalDataSource = get(),
            movieDetailsUpdates = get(),
            movieLocalDataSource = get(),
            sessionManager = get(),
        )
    }

    viewModel {
        AllActivityPersonalViewModel(
            getActivityUseCase = get(),
            showLocalDataSource = get(),
            episodeLocalDataSource = get(),
            movieLocalDataSource = get(),
            showUpdatesSource = get(),
            episodeUpdatesSource = get(),
            movieDetailsUpdates = get(),
            sessionManager = get(),
            analytics = get(),
        )
    }

    viewModel {
        AllActivitySocialViewModel(
            getActivityUseCase = get(),
            showLocalDataSource = get(),
            episodeLocalDataSource = get(),
            movieLocalDataSource = get(),
            sessionManager = get(),
            analytics = get(),
        )
    }

    viewModel {
        HomeUpcomingViewModel(
            getUpcomingUseCase = get(),
            homeUpNextSource = get(),
            showLocalDataSource = get(),
            episodeLocalDataSource = get(),
            movieLocalDataSource = get(),
            sessionManager = get(),
            modeManager = get(),
        )
    }

    viewModel {
        ActivityItemContextViewModel(
            updateMovieHistoryUseCase = get(),
            updateEpisodeHistoryUseCase = get(),
            activityLocalSource = get(),
            allActivityLocalSource = get(),
            loadUserProgressUseCase = get(),
            analytics = get(),
        )
    }

    viewModel {
        UpNextItemContextViewModel(
            updateShowHistoryUseCase = get(),
            upNextLocalDataSource = get(),
            allUpNextLocalDataSource = get(),
            upcomingLocalDataSource = get(),
            loadUserProgressUseCase = get(),
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

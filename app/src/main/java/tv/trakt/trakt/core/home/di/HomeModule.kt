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
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.core.home.watchlist.data.HomeWatchlistLocalDataSource
import tv.trakt.trakt.common.core.home.watchlist.data.HomeWatchlistStorage
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
import tv.trakt.trakt.core.home.sections.recommended.HomeRecommendedViewModel
import tv.trakt.trakt.core.home.sections.streaks.HomeStreaksViewModel
import tv.trakt.trakt.core.home.sections.streaks.all.StreaksViewModel
import tv.trakt.trakt.core.home.sections.streaks.data.DefaultStreaksManager
import tv.trakt.trakt.core.home.sections.streaks.data.StreaksManager
import tv.trakt.trakt.core.home.sections.upcoming.HomeUpcomingViewModel
import tv.trakt.trakt.core.home.sections.upcoming.data.local.HomeUpcomingLocalDataSource
import tv.trakt.trakt.core.home.sections.upcoming.data.local.HomeUpcomingStorage
import tv.trakt.trakt.core.home.sections.upcoming.usecases.GetUpcomingUseCase
import tv.trakt.trakt.core.home.sections.upnext.HomeUpNextViewModel
import tv.trakt.trakt.core.home.sections.upnext.data.local.HomeUpNextLocalDataSource
import tv.trakt.trakt.core.home.sections.upnext.data.local.HomeUpNextStorage
import tv.trakt.trakt.core.home.sections.upnext.features.all.AllHomeUpNextViewModel
import tv.trakt.trakt.core.home.sections.upnext.features.all.data.local.UpNextUpdates
import tv.trakt.trakt.core.home.sections.upnext.features.all.data.local.UpNextUpdatesStorage
import tv.trakt.trakt.core.home.sections.upnext.features.context.UpNextItemContextViewModel
import tv.trakt.trakt.core.home.sections.upnext.usecases.DropPlaybackUseCase
import tv.trakt.trakt.core.home.sections.upnext.usecases.GetUpNextUseCase
import tv.trakt.trakt.core.home.sections.watchlist.HomeWatchlistViewModel
import tv.trakt.trakt.core.home.sections.watchlist.features.all.AllHomeWatchlistViewModel
import tv.trakt.trakt.core.home.sections.watchlist.usecases.AddHomeHistoryUseCase
import tv.trakt.trakt.core.home.sections.watchlist.usecases.GetHomeMoviesWatchlistUseCase
import tv.trakt.trakt.core.home.sections.watchlist.usecases.GetHomeShowsWatchlistUseCase
import tv.trakt.trakt.core.home.sections.welcome.usecases.GetUserUsageUseCase

internal const val HOME_PREFERENCES = "home_preferences_mobile"

internal val homeDataModule = module {
    singleOf(::HomeUpNextStorage) { bind<HomeUpNextLocalDataSource>() }
    singleOf(::UpNextUpdatesStorage) { bind<UpNextUpdates>() }
    singleOf(::HomeWatchlistStorage) { bind<HomeWatchlistLocalDataSource>() }
    singleOf(::HomeUpcomingStorage) { bind<HomeUpcomingLocalDataSource>() }
    singleOf(::HomePersonalStorage) { bind<HomePersonalLocalDataSource>() }
    singleOf(::HomeSocialStorage) { bind<HomeSocialLocalDataSource>() }
    singleOf(::AllActivityStorage) { bind<AllActivityLocalDataSource>() }
    singleOf(::DefaultStreaksManager) { bind<StreaksManager>() }

    single<DataStore<Preferences>>(named(HOME_PREFERENCES)) {
        createStore(
            context = androidApplication(),
        )
    }
}

internal val homeModule = module {
    factoryOf(::GetUpNextUseCase)
    factoryOf(::DropPlaybackUseCase)
    factoryOf(::GetHomeShowsWatchlistUseCase)
    factoryOf(::GetHomeMoviesWatchlistUseCase)
    factoryOf(::AddHomeHistoryUseCase)
    factoryOf(::GetSocialActivityUseCase)
    factoryOf(::GetPersonalActivityUseCase)
    factoryOf(::GetUpcomingUseCase)
    factoryOf(::GetUserUsageUseCase)

    viewModelOf(::HomeViewModel)
    viewModelOf(::HomeSocialViewModel)
    viewModelOf(::HomeHistoryViewModel)
    viewModelOf(::AllActivityPersonalViewModel)
    viewModelOf(::AllActivitySocialViewModel)
    viewModelOf(::ActivityItemContextViewModel)
    viewModelOf(::UpNextItemContextViewModel)
    viewModelOf(::HomeStreaksViewModel)
    viewModelOf(::StreaksViewModel)

    viewModel {
        HomeUpNextViewModel(
            appContext = androidApplication(),
            getUpNextUseCase = get(),
            updateHistoryUseCase = get(),
            loadUserProgressUseCase = get(),
            appReviewUseCase = get(),
            homePersonalActivitySource = get(),
            upNextUpdates = get(),
            showUpdates = get(),
            episodeUpdates = get(),
            checkInUpdates = get(),
            watchlistUpdates = get(),
            filterManager = get(),
            sessionManager = get(),
            collapsingManager = get(),
            checkInManager = get(),
            analytics = get(),
        )
    }

    viewModel {
        AllHomeUpNextViewModel(
            appContext = androidApplication(),
            getUpNextUseCase = get(),
            updateHistoryUseCase = get(),
            loadUserProgressUseCase = get(),
            upNextUpdates = get(),
            showUpdates = get(),
            episodeUpdates = get(),
            movieUpdates = get(),
            checkInUpdates = get(),
            checkInManager = get(),
            filterManager = get(),
            sessionManager = get(),
            analytics = get(),
        )
    }

    viewModel {
        HomeWatchlistViewModel(
            appContext = androidApplication(),
            getShowsUseCase = get(),
            getMoviesUseCase = get(),
            addHistoryUseCase = get(),
            loadUserProgressUseCase = get(),
            showLocalDataSource = get(),
            movieLocalDataSource = get(),
            userWatchlistMinSource = get(),
            userWatchlistSource = get(),
            filterManager = get(),
            sessionManager = get(),
            collapsingManager = get(),
            checkInManager = get(),
            ratePromptManager = get(),
            checkInUpdates = get(),
            watchlistUpdates = get(),
            analytics = get(),
        )
    }

    viewModel {
        AllHomeWatchlistViewModel(
            appContext = androidApplication(),
            getMoviesUseCase = get(),
            getShowsUseCase = get(),
            addHistoryUseCase = get(),
            loadUserProgressUseCase = get(),
            userWatchlistSource = get(),
            userWatchlistMinSource = get(),
            showLocalDataSource = get(),
            movieLocalDataSource = get(),
            filterManager = get(),
            sessionManager = get(),
            checkInManager = get(),
            ratePromptManager = get(),
            checkInUpdates = get(),
            watchlistUpdates = get(),
            analytics = get(),
        )
    }

    viewModel {
        HomeUpcomingViewModel(
            appContext = androidApplication(),
            getUpcomingUseCase = get(),
            getCalendarTypeUseCase = get(),
            showLocalDataSource = get(),
            episodeLocalDataSource = get(),
            movieLocalDataSource = get(),
            episodeUpdates = get(),
            sessionManager = get(),
            filterManager = get(),
            collapsingManager = get(),
            watchlistUpdates = get(),
            upNextUpdates = get(),
        )
    }

    viewModel { (customTheme: Boolean) ->
        HomeRecommendedViewModel(
            filterManager = get(),
            collapsingManager = get(),
            getRecommendedShowsUseCase = when {
                customTheme -> get(named("customRecommendedShowsUseCase"))
                else -> get(named("defaultRecommendedShowsUseCase"))
            },
            getRecommendedMoviesUseCase = when {
                customTheme -> get(named("customRecommendedMoviesUseCase"))
                else -> get(named("defaultRecommendedMoviesUseCase"))
            },
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

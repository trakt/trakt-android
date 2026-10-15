package tv.trakt.trakt.core.today.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
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
import tv.trakt.trakt.core.today.data.local.TodayLocalDataSource
import tv.trakt.trakt.core.today.data.local.TodaySeenLocalDataSource
import tv.trakt.trakt.core.today.data.local.TodaySeenStorage
import tv.trakt.trakt.core.today.data.local.TodayStorage
import tv.trakt.trakt.core.today.data.remote.TodayApiClient
import tv.trakt.trakt.core.today.data.remote.TodayRemoteDataSource
import tv.trakt.trakt.core.today.model.TodayForYouItem
import tv.trakt.trakt.core.today.model.TodayMedia
import tv.trakt.trakt.core.today.pill.TodayPillViewModel
import tv.trakt.trakt.core.today.stories.TodayStoriesViewModel
import tv.trakt.trakt.core.today.stories.details.TodayPosterDetailsViewModel
import tv.trakt.trakt.core.today.stories.watched.TodayWatchedViewModel
import tv.trakt.trakt.core.today.stories.watchlist.TodayWatchlistViewModel
import tv.trakt.trakt.core.today.usecases.GetTodayDetailsUseCase
import tv.trakt.trakt.core.today.usecases.GetTodayForYouUseCase
import tv.trakt.trakt.core.today.usecases.GetTodayStoriesUseCase
import tv.trakt.trakt.core.today.usecases.UpdateTodayHistoryUseCase
import tv.trakt.trakt.core.today.usecases.UpdateTodayWatchlistUseCase

internal const val TODAY_PREFERENCES = "today_preferences_mobile"

internal val todayDataModule = module {
    single<DataStore<Preferences>>(named(TODAY_PREFERENCES)) {
        createStore(
            context = androidApplication(),
        )
    }

    singleOf(::TodayApiClient) { bind<TodayRemoteDataSource>() }
    singleOf(::TodayStorage) { bind<TodayLocalDataSource>() }

    single<TodaySeenLocalDataSource> {
        TodaySeenStorage(
            dataStore = get(named(TODAY_PREFERENCES)),
        )
    }
}

internal val todayModule = module {
    factoryOf(::GetTodayStoriesUseCase)
    factoryOf(::GetTodayDetailsUseCase)
    factoryOf(::UpdateTodayWatchlistUseCase)
    factoryOf(::GetTodayForYouUseCase)
    factoryOf(::UpdateTodayHistoryUseCase)

    viewModelOf(::TodayPillViewModel)
    viewModelOf(::TodayStoriesViewModel)
    viewModel { (media: TodayMedia) ->
        TodayPosterDetailsViewModel(
            media = media,
            getTodayDetailsUseCase = get(),
        )
    }
    viewModel { (item: TodayForYouItem) ->
        TodayWatchedViewModel(
            item = item,
            updateHistoryUseCase = get(),
            analytics = get(),
        )
    }
    viewModel { (media: TodayMedia) ->
        TodayWatchlistViewModel(
            media = media,
            updateWatchlistUseCase = get(),
            watchlistUpdates = get(),
            sessionManager = get(),
            errorsManager = get(),
            analytics = get(),
        )
    }
}

private fun createStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { emptyPreferences() },
        ),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(TODAY_PREFERENCES) },
    )
}

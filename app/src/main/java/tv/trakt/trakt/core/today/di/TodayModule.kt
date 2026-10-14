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
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.core.today.data.local.TodayLocalDataSource
import tv.trakt.trakt.core.today.data.local.TodaySeenLocalDataSource
import tv.trakt.trakt.core.today.data.local.TodaySeenStorage
import tv.trakt.trakt.core.today.data.local.TodayStorage
import tv.trakt.trakt.core.today.data.remote.TodayApiClient
import tv.trakt.trakt.core.today.data.remote.TodayRemoteDataSource
import tv.trakt.trakt.core.today.usecases.GetTodayStoriesUseCase

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

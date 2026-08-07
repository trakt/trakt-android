package tv.trakt.trakt.core.calendar.di

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
import tv.trakt.trakt.core.calendar.feature.monthly.CalendarMonthlyViewModel
import tv.trakt.trakt.core.calendar.feature.monthly.data.CalendarMonthlyItemsCache
import tv.trakt.trakt.core.calendar.feature.monthly.usecases.GetMonthlyCalendarItemsUseCase
import tv.trakt.trakt.core.calendar.feature.weekly.CalendarViewModel
import tv.trakt.trakt.core.calendar.feature.weekly.usecases.GetCalendarItemsUseCase
import tv.trakt.trakt.core.calendar.usecases.CalendarItemsLoader
import tv.trakt.trakt.core.calendar.usecases.GetCalendarDisplayUseCase
import tv.trakt.trakt.core.calendar.usecases.GetCalendarTypeUseCase
import tv.trakt.trakt.core.calendar.usecases.GetCalendarViewUseCase
import tv.trakt.trakt.core.calendar.usecases.ObserveCalendarUpdatesUseCase
import tv.trakt.trakt.core.calendar.usecases.SaveCalendarMediaUseCase
import tv.trakt.trakt.core.calendar.usecases.UpdateCalendarHistoryUseCase

internal const val CALENDAR_PREFERENCES = "calendar_preferences_mobile"

internal val calendarModule = module {
    single<DataStore<Preferences>>(named(CALENDAR_PREFERENCES)) {
        createStore(
            context = androidApplication(),
        )
    }

    factory {
        GetCalendarTypeUseCase(
            dataStore = get(named(CALENDAR_PREFERENCES)),
        )
    }

    factory {
        GetCalendarViewUseCase(
            dataStore = get(named(CALENDAR_PREFERENCES)),
        )
    }

    factory {
        GetCalendarDisplayUseCase(
            dataStore = get(named(CALENDAR_PREFERENCES)),
        )
    }

    factoryOf(::CalendarMonthlyItemsCache)
    factoryOf(::CalendarItemsLoader)
    factoryOf(::GetCalendarItemsUseCase)
    factoryOf(::GetMonthlyCalendarItemsUseCase)
    factoryOf(::SaveCalendarMediaUseCase)
    factoryOf(::ObserveCalendarUpdatesUseCase)
    factoryOf(::UpdateCalendarHistoryUseCase)

    viewModelOf(::CalendarViewModel)
    viewModelOf(::CalendarMonthlyViewModel)
}

private fun createStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { emptyPreferences() },
        ),
        migrations = listOf(SharedPreferencesMigration(context, CALENDAR_PREFERENCES)),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(CALENDAR_PREFERENCES) },
    )
}

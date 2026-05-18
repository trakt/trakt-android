package tv.trakt.trakt.core.filters.di

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
import tv.trakt.trakt.core.filters.GlobalFiltersViewModel
import tv.trakt.trakt.core.filters.data.DefaultGlobalFilterManager
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.filters.navigation.GlobalFiltersOptions

internal const val FILTERS_PREFERENCES = "filters_preferences"

internal val filtersModule = module {
    single<DataStore<Preferences>>(named(FILTERS_PREFERENCES)) {
        createStore(
            context = androidApplication(),
        )
    }

    single<GlobalFilterManager> {
        DefaultGlobalFilterManager(
            dataStore = get(named(FILTERS_PREFERENCES)),
        )
    }

    viewModel { (options: GlobalFiltersOptions) ->
        GlobalFiltersViewModel(
            options = options,
            filterManager = get(),
        )
    }
}

private fun createStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { emptyPreferences() },
        ),
        migrations = listOf(SharedPreferencesMigration(context, FILTERS_PREFERENCES)),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(FILTERS_PREFERENCES) },
    )
}

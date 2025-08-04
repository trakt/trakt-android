package tv.trakt.trakt.app.core.main.di

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
import tv.trakt.trakt.app.core.main.MainViewModel

private const val MAIN_PREFERENCES = "main_preferences"

internal val mainModule = module {
    single<DataStore<Preferences>>(named(MAIN_PREFERENCES)) {
        createStore(
            context = androidContext(),
            key = MAIN_PREFERENCES,
        )
    }

    viewModel {
        MainViewModel(
            sessionManager = get(),
            mainDataStore = get(named(MAIN_PREFERENCES)),
            loadUserProfileUseCase = get(),
        )
    }
}

private fun createStore(
    context: Context,
    key: String,
): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { emptyPreferences() },
        ),
        migrations = listOf(SharedPreferencesMigration(context, key)),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(key) },
    )
}

package tv.trakt.trakt.core.auth.di

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
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.core.auth.data.remote.AuthApiClient
import tv.trakt.trakt.core.auth.data.remote.AuthRemoteDataSource
import tv.trakt.trakt.core.auth.usecase.AuthorizeUserUseCase

internal const val AUTH_PREFERENCES = "auth_preferences_mobile"

val authModule = module {
    single<DataStore<Preferences>>(named(AUTH_PREFERENCES)) {
        createStore(
            context = androidApplication(),
        )
    }

    single<AuthRemoteDataSource> {
        AuthApiClient(
            api = get(),
        )
    }

    factory {
        AuthorizeUserUseCase(
            tokenProvider = get(),
            remoteSource = get(),
        )
    }
}

private fun createStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { emptyPreferences() },
        ),
        migrations = listOf(SharedPreferencesMigration(context, AUTH_PREFERENCES)),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(AUTH_PREFERENCES) },
    )
}

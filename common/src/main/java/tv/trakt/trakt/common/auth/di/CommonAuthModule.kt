package tv.trakt.trakt.common.auth.di

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
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.auth.DefaultTokenProvider
import tv.trakt.trakt.common.auth.TokenProvider
import tv.trakt.trakt.common.auth.session.DefaultSessionManager
import tv.trakt.trakt.common.auth.session.SessionManager

private const val AUTH_PREFERENCES = "auth_preferences"
private const val SESSION_PREFERENCES = "session_preferences"

val commonAuthModule = module {
    single<DataStore<Preferences>>(named(AUTH_PREFERENCES)) {
        createStore(
            context = androidContext(),
            key = AUTH_PREFERENCES,
        )
    }

    single<DataStore<Preferences>>(named(SESSION_PREFERENCES)) {
        createStore(
            context = androidContext(),
            key = SESSION_PREFERENCES,
        )
    }

    single<TokenProvider> {
        DefaultTokenProvider(
            dataStore = get(named(AUTH_PREFERENCES)),
        )
    }

    single<SessionManager> {
        DefaultSessionManager(
            tokenProvider = get(),
            dataStore = get(named(SESSION_PREFERENCES)),
        )
    }
}

internal fun createStore(
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

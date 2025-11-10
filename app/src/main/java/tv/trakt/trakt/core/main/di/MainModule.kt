package tv.trakt.trakt.core.main.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.BuildConfig
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.analytics.implementation.DebugAnalytics
import tv.trakt.trakt.analytics.implementation.DebugAnalyticsProgress
import tv.trakt.trakt.analytics.implementation.DebugAnalyticsRatings
import tv.trakt.trakt.analytics.implementation.DebugAnalyticsReactions
import tv.trakt.trakt.analytics.implementation.FirebaseAnalytics
import tv.trakt.trakt.analytics.implementation.FirebaseAnalyticsProgress
import tv.trakt.trakt.analytics.implementation.FirebaseAnalyticsRatings
import tv.trakt.trakt.analytics.implementation.FirebaseAnalyticsReactions
import tv.trakt.trakt.core.auth.di.AUTH_PREFERENCES
import tv.trakt.trakt.core.main.MainViewModel
import tv.trakt.trakt.core.main.helpers.DefaultMediaModeManager
import tv.trakt.trakt.core.main.helpers.MediaModeManager
import tv.trakt.trakt.core.main.usecases.DismissWelcomeUseCase
import tv.trakt.trakt.core.main.usecases.HalloweenUseCase

internal const val MAIN_PREFERENCES = "main_preferences"

internal val mainModule = module {
    single<DataStore<Preferences>>(named(MAIN_PREFERENCES)) {
        createStore(
            context = androidApplication(),
        )
    }

    single<MediaModeManager> {
        DefaultMediaModeManager(
            dataStore = get(named(MAIN_PREFERENCES)),
        )
    }

    single<Analytics> {
        if (BuildConfig.DEBUG) {
            DebugAnalytics(
                reactions = DebugAnalyticsReactions(),
                ratings = DebugAnalyticsRatings(),
                progress = DebugAnalyticsProgress(),
            )
        } else {
            val firebase = Firebase.analytics
            FirebaseAnalytics(
                firebase = firebase,
                reactions = FirebaseAnalyticsReactions(
                    firebase = firebase,
                ),
                ratings = FirebaseAnalyticsRatings(
                    firebase = firebase,
                ),
                progress = FirebaseAnalyticsProgress(
                    firebase = firebase,
                ),
            )
        }
    }

    viewModel {
        MainViewModel(
            sessionManager = get(),
            loadUserProgressUseCase = get(),
            loadUserWatchlistUseCase = get(),
            loadUserRatingsUseCase = get(),
            authorizePreferences = get(named(AUTH_PREFERENCES)),
            authorizeUseCase = get(),
            getUserUseCase = get(),
            logoutUserUseCase = get(),
            dismissWelcomeUseCase = get(),
            analytics = get(),
        )
    }

    factory {
        DismissWelcomeUseCase(
            mainDataStore = get(named(MAIN_PREFERENCES)),
        )
    }

    factory {
        HalloweenUseCase(
            mainDataStore = get(named(MAIN_PREFERENCES)),
        )
    }
}

private fun createStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { emptyPreferences() },
        ),
        migrations = listOf(SharedPreferencesMigration(context, MAIN_PREFERENCES)),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(MAIN_PREFERENCES) },
    )
}

package tv.trakt.trakt.core.main.di

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
import tv.trakt.trakt.common.helpers.errors.DefaultGlobalErrorsManager
import tv.trakt.trakt.common.helpers.errors.GlobalErrorsManager
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider
import tv.trakt.trakt.common.helpers.lifecycle.DefaultAppLifecycleProvider
import tv.trakt.trakt.core.auth.di.AUTH_PREFERENCES
import tv.trakt.trakt.core.main.MainViewModel
import tv.trakt.trakt.core.main.usecases.CustomThemeUseCase
import tv.trakt.trakt.core.main.usecases.DismissWelcomeUseCase
import tv.trakt.trakt.core.main.usecases.LoadWhatsNewUseCase
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.DefaultCollapsingManager

internal const val MAIN_PREFERENCES = "main_preferences"
internal const val WHATS_NEW_PREFERENCES = "whats_new_preferences"
internal const val COLLAPSING_PREFERENCES = "collapsing_preferences"

internal val mainModule = module {
    single<DataStore<Preferences>>(named(MAIN_PREFERENCES)) {
        createStore(
            context = androidApplication(),
            file = MAIN_PREFERENCES,
        )
    }

    single<DataStore<Preferences>>(named(COLLAPSING_PREFERENCES)) {
        createStore(
            context = androidApplication(),
            file = COLLAPSING_PREFERENCES,
        )
    }

    single<DataStore<Preferences>>(named(WHATS_NEW_PREFERENCES)) {
        createStore(
            context = androidApplication(),
            file = WHATS_NEW_PREFERENCES,
        )
    }

    single<CollapsingManager> {
        DefaultCollapsingManager(
            dataStore = get(named(COLLAPSING_PREFERENCES)),
        )
    }

    single<AppLifecycleProvider> {
        DefaultAppLifecycleProvider()
    }

    single<GlobalErrorsManager> {
        DefaultGlobalErrorsManager()
    }

    viewModel {
        MainViewModel(
            appContext = androidApplication(),
            sessionManager = get(),
            checkInManager = get(),
            ratePromptManager = get(),
            loadUserProgressUseCase = get(),
            loadUserWatchlistUseCase = get(),
            loadUserListsUseCase = get(),
            loadUserRatingsUseCase = get(),
            authorizePreferences = get(named(AUTH_PREFERENCES)),
            authorizeUseCase = get(),
            getUserUseCase = get(),
            logoutUseCase = get(),
            dismissWelcomeUseCase = get(),
            loadWhatsNewUseCase = get(),
            inAppReviewUseCase = get(),
            errorsManager = get(),
            analytics = get(),
        )
    }

    factory {
        DismissWelcomeUseCase(
            mainDataStore = get(named(MAIN_PREFERENCES)),
        )
    }

    factory {
        LoadWhatsNewUseCase(
            dataStore = get(named(WHATS_NEW_PREFERENCES)),
        )
    }

    factory {
        CustomThemeUseCase(
            mainDataStore = get(named(MAIN_PREFERENCES)),
            sessionManager = get(),
        )
    }
}

private fun createStore(
    context: Context,
    file: String,
): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { emptyPreferences() },
        ),
        migrations = listOf(SharedPreferencesMigration(context, file)),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(file) },
    )
}

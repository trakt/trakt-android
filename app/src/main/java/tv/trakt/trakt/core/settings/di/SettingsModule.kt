package tv.trakt.trakt.core.settings.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import io.ktor.client.HttpClientConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.Config.API_BASE_URL
import tv.trakt.trakt.core.settings.SettingsViewModel
import tv.trakt.trakt.core.settings.features.younify.YounifyViewModel
import tv.trakt.trakt.core.settings.features.younify.data.remote.YounifyApiClient
import tv.trakt.trakt.core.settings.features.younify.data.remote.YounifyRemoteDataSource
import tv.trakt.trakt.core.settings.features.younify.usecases.GetYounifyDetailsUseCase
import tv.trakt.trakt.core.settings.features.younify.usecases.RefreshYounifyDataUseCase
import tv.trakt.trakt.core.settings.features.younify.usecases.RefreshYounifyTokensUseCase
import tv.trakt.trakt.core.settings.features.younify.usecases.UnlinkYounifyServiceUseCase
import tv.younify.sdk.connect.Connect

internal const val SETTINGS_PREFERENCES = "settings_preferences_mobile"

internal val settingsDataModule = module {
    single<YounifyRemoteDataSource> {
        YounifyApiClient(
            baseUrl = API_BASE_URL,
            httpClientEngine = get(),
            httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("authorizedClientConfig")),
        )
    }
}

internal val settingsModule = module {

    factory {
        GetYounifyDetailsUseCase(
            remoteSource = get(),
        )
    }

    factory {
        RefreshYounifyTokensUseCase(
            remoteSource = get(),
        )
    }

    factory {
        RefreshYounifyDataUseCase(
            remoteSource = get(),
        )
    }

    factory {
        UnlinkYounifyServiceUseCase(
            remoteSource = get(),
        )
    }

    viewModel {
        SettingsViewModel(
            sessionManager = get(),
            analytics = get(),
            logoutUseCase = get(),
        )
    }

    viewModel {
        YounifyViewModel(
            younify = Connect,
            sessionManager = get(),
            analytics = get(),
            getYounifyDetailsUseCase = get(),
            refreshYounifyTokensUseCase = get(),
            refreshYounifyDataUseCase = get(),
            unlinkYounifyServiceUseCase = get(),
        )
    }
}

private fun createStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { emptyPreferences() },
        ),
        migrations = listOf(SharedPreferencesMigration(context, SETTINGS_PREFERENCES)),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(SETTINGS_PREFERENCES) },
    )
}

package tv.trakt.trakt.app.core.streamings.di

import android.annotation.SuppressLint
import android.provider.Settings
import androidx.lifecycle.SavedStateHandle
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.app.core.plex.data.PlexTimelineClient
import tv.trakt.trakt.app.core.streamings.AllStreamingsViewModel

@SuppressLint("HardwareIds")
internal val plexDataModule = module {
    single {
        PlexTimelineClient(
            httpClientEngine = get(),
            // Stable per device + signing key, survives reinstall. Plex only needs it to tell sessions apart.
            clientIdentifier = Settings.Secure
                .getString(androidContext().contentResolver, Settings.Secure.ANDROID_ID)
                .orEmpty(),
        )
    }
}

internal val allStreamingsModule = module {
    viewModel { (stateHandle: SavedStateHandle) ->
        AllStreamingsViewModel(
            savedStateHandle = stateHandle,
            sessionManager = get(),
            getAllStreamingsUseCase = get(),
            getYouTubeSpecialUseCase = get(),
        )
    }
}

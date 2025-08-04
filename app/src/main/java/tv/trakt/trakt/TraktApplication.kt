package tv.trakt.trakt

import android.app.Application
import android.app.UiModeManager
import android.content.res.Configuration.UI_MODE_TYPE_TELEVISION
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import tv.trakt.trakt.app.TvActivity
import tv.trakt.trakt.common.auth.di.commonAuthModule
import tv.trakt.trakt.common.networking.di.networkingModule
import tv.trakt.trakt.core.shows.di.showsDataModule
import tv.trakt.trakt.core.shows.di.showsModule
import java.util.concurrent.TimeUnit.MINUTES

internal class TraktApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupKoin()

        FirebaseApp.initializeApp(this)
        setupFirebaseConfig()
    }

    fun setupFirebaseConfig() {
        with(Firebase.remoteConfig) {
            setDefaultsAsync(R.xml.remote_config_defaults)
            setConfigSettingsAsync(
                remoteConfigSettings {
                    minimumFetchIntervalInSeconds = when {
                        BuildConfig.DEBUG -> 0
                        else -> MINUTES.toSeconds(30)
                    }
                },
            )
        }
    }

    fun setupKoin() {
        val uiModeManager = getSystemService(UI_MODE_SERVICE) as UiModeManager
        if (uiModeManager.currentModeType == UI_MODE_TYPE_TELEVISION) {
            TvActivity.setupKoin(this)
            return
        }

        startKoin {
            androidContext(applicationContext)
            androidLogger()
            modules(
                networkingModule,
                commonAuthModule,
                showsModule,
                showsDataModule,
            )
        }
    }
}

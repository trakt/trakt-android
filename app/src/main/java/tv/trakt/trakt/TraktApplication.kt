package tv.trakt.trakt

import android.app.Application
import android.app.UiModeManager
import android.content.res.Configuration.UI_MODE_TYPE_TELEVISION
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber
import tv.trakt.trakt.app.TvActivity
import tv.trakt.trakt.common.auth.di.commonAuthModule
import tv.trakt.trakt.common.networking.di.networkingModule
import tv.trakt.trakt.core.movies.di.moviesDataModule
import tv.trakt.trakt.core.movies.di.moviesModule
import tv.trakt.trakt.core.profile.di.profileModule
import tv.trakt.trakt.core.shows.di.showsDataModule
import tv.trakt.trakt.core.shows.di.showsModule
import java.util.concurrent.TimeUnit.MINUTES

internal class TraktApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupKoin()
        setupTimber()

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
                moviesModule,
                moviesDataModule,
                profileModule,
            )
        }
    }

    fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}

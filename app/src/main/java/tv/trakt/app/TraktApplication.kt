package tv.trakt.app

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import tv.trakt.app.tv.TvActivity
import java.util.concurrent.TimeUnit.MINUTES

class TraktApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        TvActivity.setupKoin(this)
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
}

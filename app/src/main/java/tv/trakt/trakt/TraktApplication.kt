package tv.trakt.trakt

import android.app.Application
import android.app.UiModeManager
import android.content.res.Configuration.UI_MODE_TYPE_TELEVISION
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber
import tv.trakt.trakt.app.TvActivity
import tv.trakt.trakt.common.auth.di.commonAuthModule
import tv.trakt.trakt.common.networking.di.networkingApiModule
import tv.trakt.trakt.common.networking.di.networkingModule
import tv.trakt.trakt.core.auth.di.authModule
import tv.trakt.trakt.core.comments.di.commentsDataModule
import tv.trakt.trakt.core.comments.di.commentsModule
import tv.trakt.trakt.core.home.di.homeDataModule
import tv.trakt.trakt.core.home.di.homeModule
import tv.trakt.trakt.core.lists.di.listsDataModule
import tv.trakt.trakt.core.lists.di.listsModule
import tv.trakt.trakt.core.main.di.mainModule
import tv.trakt.trakt.core.movies.di.moviesDataModule
import tv.trakt.trakt.core.movies.di.moviesModule
import tv.trakt.trakt.core.people.di.peopleDataModule
import tv.trakt.trakt.core.search.di.searchDataModule
import tv.trakt.trakt.core.search.di.searchModule
import tv.trakt.trakt.core.shows.di.showsDataModule
import tv.trakt.trakt.core.shows.di.showsModule
import tv.trakt.trakt.core.streamings.di.streamingsDataModule
import tv.trakt.trakt.core.summary.movies.di.movieDetailsDataModule
import tv.trakt.trakt.core.summary.movies.di.movieDetailsModule
import tv.trakt.trakt.core.summary.shows.di.showDetailsDataModule
import tv.trakt.trakt.core.summary.shows.di.showDetailsModule
import tv.trakt.trakt.core.sync.di.syncModule
import tv.trakt.trakt.core.user.di.profileDataModule
import tv.trakt.trakt.core.user.di.profileModule
import java.util.concurrent.TimeUnit.MINUTES

internal class TraktApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setupKoin()
        setupTimber()

        FirebaseApp.initializeApp(this)
        setupFirebaseConfig()
        setupFirebaseCrashlytics()
    }

    fun setupFirebaseCrashlytics() {
        Firebase.crashlytics.isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG
    }

    fun setupFirebaseConfig() {
        with(Firebase.remoteConfig) {
            setDefaultsAsync(R.xml.remote_config_defaults)
            setConfigSettingsAsync(
                remoteConfigSettings {
                    minimumFetchIntervalInSeconds = when {
                        BuildConfig.DEBUG -> 0
                        else -> MINUTES.toSeconds(10)
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
                networkingApiModule,
                mainModule,
                authModule,
                commonAuthModule,
                homeModule,
                homeDataModule,
                showsModule,
                showsDataModule,
                showDetailsModule,
                showDetailsDataModule,
                moviesModule,
                moviesDataModule,
                movieDetailsModule,
                movieDetailsDataModule,
                profileModule,
                profileDataModule,
                peopleDataModule,
                searchModule,
                searchDataModule,
                streamingsDataModule,
                commentsDataModule,
                commentsModule,
                listsModule,
                listsDataModule,
                syncModule,
            )
        }
    }

    fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}

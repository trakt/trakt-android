package tv.trakt.trakt

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.jakewharton.processphoenix.ProcessPhoenix
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import timber.log.Timber
import tv.trakt.trakt.app.TvActivity
import tv.trakt.trakt.common.auth.di.commonAuthModule
import tv.trakt.trakt.common.helpers.extensions.isTelevision
import tv.trakt.trakt.common.networking.di.networkingApiModule
import tv.trakt.trakt.common.networking.di.networkingModule
import tv.trakt.trakt.core.auth.di.authModule
import tv.trakt.trakt.core.billing.di.billingDataModule
import tv.trakt.trakt.core.billing.di.billingModule
import tv.trakt.trakt.core.comments.di.commentsDataModule
import tv.trakt.trakt.core.comments.di.commentsModule
import tv.trakt.trakt.core.discover.di.discoverModule
import tv.trakt.trakt.core.favorites.di.favoritesDataModule
import tv.trakt.trakt.core.home.di.homeDataModule
import tv.trakt.trakt.core.home.di.homeModule
import tv.trakt.trakt.core.lists.di.listsDataModule
import tv.trakt.trakt.core.lists.di.listsModule
import tv.trakt.trakt.core.main.di.mainModule
import tv.trakt.trakt.core.movies.di.moviesDataModule
import tv.trakt.trakt.core.movies.di.moviesModule
import tv.trakt.trakt.core.people.di.peopleDataModule
import tv.trakt.trakt.core.people.di.peopleModule
import tv.trakt.trakt.core.profile.di.profileDataModule
import tv.trakt.trakt.core.profile.di.profileModule
import tv.trakt.trakt.core.ratings.di.ratingsDataModule
import tv.trakt.trakt.core.ratings.di.ratingsModule
import tv.trakt.trakt.core.reactions.di.reactionsDataModule
import tv.trakt.trakt.core.reactions.di.reactionsModule
import tv.trakt.trakt.core.search.di.searchDataModule
import tv.trakt.trakt.core.search.di.searchModule
import tv.trakt.trakt.core.settings.di.settingsDataModule
import tv.trakt.trakt.core.settings.di.settingsModule
import tv.trakt.trakt.core.shows.di.showsDataModule
import tv.trakt.trakt.core.shows.di.showsModule
import tv.trakt.trakt.core.streamings.di.streamingsDataModule
import tv.trakt.trakt.core.summary.episodes.di.episodeDetailsDataModule
import tv.trakt.trakt.core.summary.episodes.di.episodeDetailsModule
import tv.trakt.trakt.core.summary.movies.di.movieDetailsDataModule
import tv.trakt.trakt.core.summary.movies.di.movieDetailsModule
import tv.trakt.trakt.core.summary.people.di.personDetailsModule
import tv.trakt.trakt.core.summary.shows.di.showDetailsDataModule
import tv.trakt.trakt.core.summary.shows.di.showDetailsModule
import tv.trakt.trakt.core.sync.di.syncModule
import java.util.concurrent.TimeUnit.MINUTES

internal class TraktApplication : Application() {
    override fun onCreate() {
        if (ProcessPhoenix.isPhoenixProcess(this)) {
            return
        }

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
        if (isTelevision()) {
            TvActivity.setupKoin(this)
            return
        }

        startKoin {
            androidContext(applicationContext)
            androidLogger()

            workManagerFactory()

            modules(
                networkingModule,
                networkingApiModule,
                mainModule,
                authModule,
                commonAuthModule,
                homeModule,
                homeDataModule,
                discoverModule,
                showsModule,
                showsDataModule,
                showDetailsModule,
                showDetailsDataModule,
                episodeDetailsModule,
                episodeDetailsDataModule,
                moviesModule,
                moviesDataModule,
                movieDetailsModule,
                movieDetailsDataModule,
                profileModule,
                profileDataModule,
                peopleDataModule,
                peopleModule,
                personDetailsModule,
                searchModule,
                searchDataModule,
                streamingsDataModule,
                commentsDataModule,
                commentsModule,
                listsModule,
                listsDataModule,
                reactionsDataModule,
                reactionsModule,
                ratingsDataModule,
                ratingsModule,
                favoritesDataModule,
                syncModule,
                settingsModule,
                settingsDataModule,
                billingModule,
                billingDataModule,
            )
        }
    }

    fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}

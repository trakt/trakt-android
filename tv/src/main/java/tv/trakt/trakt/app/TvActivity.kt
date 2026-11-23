package tv.trakt.trakt.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.compose.koinViewModel
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import tv.trakt.trakt.app.core.auth.di.authModule
import tv.trakt.trakt.app.core.comments.di.commentsModule
import tv.trakt.trakt.app.core.details.episode.di.episodeDetailsModule
import tv.trakt.trakt.app.core.details.lists.details.media.di.customListMediaModule
import tv.trakt.trakt.app.core.details.movie.di.movieDetailsModule
import tv.trakt.trakt.app.core.details.ratings.di.detailsRatingsModule
import tv.trakt.trakt.app.core.details.show.di.showDetailsModule
import tv.trakt.trakt.app.core.discover.di.discoverModule
import tv.trakt.trakt.app.core.home.di.homeModule
import tv.trakt.trakt.app.core.lists.di.listsModule
import tv.trakt.trakt.app.core.main.MainScreen
import tv.trakt.trakt.app.core.main.di.mainModule
import tv.trakt.trakt.app.core.movies.di.moviesModule
import tv.trakt.trakt.app.core.people.di.personDetailsModule
import tv.trakt.trakt.app.core.profile.di.profileModule
import tv.trakt.trakt.app.core.shows.di.showsModule
import tv.trakt.trakt.app.core.streamings.di.allStreamingsModule
import tv.trakt.trakt.app.core.streamings.di.plexDataModule
import tv.trakt.trakt.app.core.sync.di.syncModule
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.auth.di.commonAuthModule
import tv.trakt.trakt.common.core.lists.di.manageListsModule
import tv.trakt.trakt.common.core.search.di.commonSearchModule
import tv.trakt.trakt.common.core.streamings.di.streamingsDataModule
import tv.trakt.trakt.common.core.streamings.di.streamingsModule
import tv.trakt.trakt.common.core.sync.changes.di.dataChangesModule
import tv.trakt.trakt.common.core.translations.di.translationsModule
import tv.trakt.trakt.common.core.tutorials.di.tutorialsModule
import tv.trakt.trakt.common.data.local.di.localSourcesModule
import tv.trakt.trakt.common.data.remote.di.remoteSourcesModule
import tv.trakt.trakt.common.firebase.analytics.di.analyticsModule
import tv.trakt.trakt.common.firebase.config.RemoteConfigManager
import tv.trakt.trakt.common.firebase.config.di.remoteConfigModule
import tv.trakt.trakt.common.firebase.inappreview.di.inAppReviewModule
import tv.trakt.trakt.common.helpers.coil.coilModule
import tv.trakt.trakt.common.helpers.coil.registerCoilImageLoader
import tv.trakt.trakt.common.helpers.extensions.isTelevision
import tv.trakt.trakt.common.networking.di.networkingApiModule
import tv.trakt.trakt.common.networking.di.networkingModule

internal val LocalDrawerVisibility = compositionLocalOf { mutableStateOf(true) }
internal val LocalSnackbarState = compositionLocalOf { SnackbarHostState() }

class TvActivity : ComponentActivity() {
    private val remoteConfigManager: RemoteConfigManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val drawerVisibility = remember { mutableStateOf(true) }
            val snackbarState = remember { SnackbarHostState() }

            TraktTheme {
                CompositionLocalProvider(
                    LocalDrawerVisibility provides drawerVisibility,
                    LocalSnackbarState provides snackbarState,
                ) {
                    MainScreen(
                        viewModel = koinViewModel(),
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        remoteConfigManager.refresh()
    }

    companion object {
        fun setupKoin(context: Context) {
            if (!context.isTelevision()) {
                return
            }

            startKoin {
                androidContext(context.applicationContext)
                androidLogger()

                workManagerFactory()

                modules(
                    networkingModule,
                    networkingApiModule,
                    coilModule,
                    mainModule,
                    tutorialsModule,
                    homeModule,
                    authModule,
                    commonAuthModule,
                    showsModule,
                    showDetailsModule,
                    moviesModule,
                    discoverModule,
                    dataChangesModule,
                    movieDetailsModule,
                    episodeDetailsModule,
                    detailsRatingsModule,
                    listsModule,
                    manageListsModule,
                    commonSearchModule,
                    commentsModule,
                    remoteSourcesModule,
                    localSourcesModule,
                    personDetailsModule,
                    customListMediaModule,
                    profileModule,
                    streamingsModule,
                    streamingsDataModule,
                    allStreamingsModule,
                    plexDataModule,
                    syncModule,
                    inAppReviewModule,
                    analyticsModule,
                    remoteConfigModule,
                    translationsModule,
                )
            }

            registerCoilImageLoader()
        }
    }
}

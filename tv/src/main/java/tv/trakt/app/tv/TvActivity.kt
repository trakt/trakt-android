package tv.trakt.app.tv

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration.UI_MODE_TYPE_TELEVISION
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.compose.koinViewModel
import org.koin.core.context.startKoin
import tv.trakt.app.tv.auth.di.baseAuthModule
import tv.trakt.app.tv.core.auth.di.authDataModule
import tv.trakt.app.tv.core.auth.di.authModule
import tv.trakt.app.tv.core.comments.di.commentsDataModule
import tv.trakt.app.tv.core.comments.di.commentsModule
import tv.trakt.app.tv.core.details.episode.di.episodeDetailsModule
import tv.trakt.app.tv.core.details.lists.details.movies.di.customListMoviesModule
import tv.trakt.app.tv.core.details.lists.details.shows.di.customListShowsModule
import tv.trakt.app.tv.core.details.lists.module.customListsDataModule
import tv.trakt.app.tv.core.details.movie.di.movieDetailsModule
import tv.trakt.app.tv.core.details.show.di.showDetailsModule
import tv.trakt.app.tv.core.episodes.di.episodesDataModule
import tv.trakt.app.tv.core.home.di.homeModule
import tv.trakt.app.tv.core.lists.di.listsModule
import tv.trakt.app.tv.core.main.MainScreen
import tv.trakt.app.tv.core.main.di.mainModule
import tv.trakt.app.tv.core.movies.di.moviesDataModule
import tv.trakt.app.tv.core.movies.di.moviesModule
import tv.trakt.app.tv.core.people.di.peopleDataModule
import tv.trakt.app.tv.core.people.di.personDetailsModule
import tv.trakt.app.tv.core.profile.di.profileDataModule
import tv.trakt.app.tv.core.profile.di.profileModule
import tv.trakt.app.tv.core.shows.di.showsDataModule
import tv.trakt.app.tv.core.shows.di.showsModule
import tv.trakt.app.tv.core.streamings.di.streamingsModule
import tv.trakt.app.tv.core.sync.di.syncModule
import tv.trakt.app.tv.networking.di.networkingModule
import tv.trakt.app.tv.ui.theme.TraktTheme

internal val LocalDrawerVisibility = compositionLocalOf { mutableStateOf(true) }
internal val LocalSnackbarState = compositionLocalOf { SnackbarHostState() }

class TvActivity : ComponentActivity() {
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
        updateRemoteConfig()
    }

    private fun updateRemoteConfig() {
        Firebase.remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("TvActivity", "Remote Config params updated: ${task.result}")
            } else {
                Log.w("TvActivity", "Remote Config fetch failed!")
            }
        }
    }

    companion object {
        fun setupKoin(context: Context) {
            val uiModeManager = context.getSystemService(UI_MODE_SERVICE) as UiModeManager
            if (uiModeManager.currentModeType != UI_MODE_TYPE_TELEVISION) {
                return
            }

            startKoin {
                androidContext(context.applicationContext)
                androidLogger()
                modules(
                    networkingModule,
                    mainModule,
                    homeModule,
                    authModule,
                    authDataModule,
                    baseAuthModule,
                    showsModule,
                    showsDataModule,
                    showDetailsModule,
                    moviesModule,
                    moviesDataModule,
                    movieDetailsModule,
                    episodesDataModule,
                    episodeDetailsModule,
                    listsModule,
                    commentsModule,
                    commentsDataModule,
                    peopleDataModule,
                    personDetailsModule,
                    customListsDataModule,
                    customListMoviesModule,
                    customListShowsModule,
                    profileDataModule,
                    profileModule,
                    streamingsModule,
                    syncModule,
                )
            }
        }
    }
}

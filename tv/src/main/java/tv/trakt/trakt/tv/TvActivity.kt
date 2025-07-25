package tv.trakt.trakt.tv

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
import tv.trakt.trakt.common.auth.di.commonAuthModule
import tv.trakt.trakt.tv.auth.di.tvAuthModule
import tv.trakt.trakt.tv.core.auth.di.authDataModule
import tv.trakt.trakt.tv.core.auth.di.authModule
import tv.trakt.trakt.tv.core.comments.di.commentsDataModule
import tv.trakt.trakt.tv.core.comments.di.commentsModule
import tv.trakt.trakt.tv.core.details.episode.di.episodeDetailsModule
import tv.trakt.trakt.tv.core.details.lists.details.movies.di.customListMoviesModule
import tv.trakt.trakt.tv.core.details.lists.details.shows.di.customListShowsModule
import tv.trakt.trakt.tv.core.details.lists.module.customListsDataModule
import tv.trakt.trakt.tv.core.details.movie.di.movieDetailsModule
import tv.trakt.trakt.tv.core.details.show.di.showDetailsModule
import tv.trakt.trakt.tv.core.episodes.di.episodesDataModule
import tv.trakt.trakt.tv.core.home.di.homeModule
import tv.trakt.trakt.tv.core.lists.di.listsModule
import tv.trakt.trakt.tv.core.main.MainScreen
import tv.trakt.trakt.tv.core.main.di.mainModule
import tv.trakt.trakt.tv.core.movies.di.moviesDataModule
import tv.trakt.trakt.tv.core.movies.di.moviesModule
import tv.trakt.trakt.tv.core.people.di.peopleDataModule
import tv.trakt.trakt.tv.core.people.di.personDetailsModule
import tv.trakt.trakt.tv.core.profile.di.profileDataModule
import tv.trakt.trakt.tv.core.profile.di.profileModule
import tv.trakt.trakt.tv.core.shows.di.showsDataModule
import tv.trakt.trakt.tv.core.shows.di.showsModule
import tv.trakt.trakt.tv.core.streamings.di.streamingsModule
import tv.trakt.trakt.tv.core.sync.di.syncModule
import tv.trakt.trakt.tv.networking.di.networkingModule
import tv.trakt.trakt.tv.ui.theme.TraktTheme

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
                    commonAuthModule,
                    tvAuthModule,
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

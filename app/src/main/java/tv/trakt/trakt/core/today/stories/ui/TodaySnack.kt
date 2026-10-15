package tv.trakt.trakt.core.today.stories.ui

import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

private val SnackDuration = 1.5.seconds

internal suspend fun SnackbarHostState.showTodaySnack(message: String) {
    withTimeoutOrNull(SnackDuration) {
        showSnackbar(message)
    }
}

package tv.trakt.trakt.core.comments.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
import org.koin.compose.koinInject
import tv.trakt.trakt.common.firebase.analytics.Analytics

@Composable
internal fun rememberSpoilerRevealLogger(): () -> Unit {
    val analytics = when {
        LocalInspectionMode.current -> null
        else -> koinInject<Analytics>()
    }

    return remember(analytics) {
        { analytics?.comments?.logSpoilerReveal() }
    }
}

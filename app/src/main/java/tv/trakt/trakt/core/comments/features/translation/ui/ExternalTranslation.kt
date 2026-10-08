package tv.trakt.trakt.core.comments.features.translation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import org.koin.compose.koinInject
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.extensions.googleTranslateActivityInfo
import tv.trakt.trakt.common.helpers.extensions.openGoogleTranslate

/**
 * Returns an action that opens the given text in Google Translate, when installed.
 */
@Composable
internal fun rememberExternalTranslation(): (String) -> Unit {
    val context = LocalContext.current
    val analytics = when {
        LocalInspectionMode.current -> null
        else -> koinInject<Analytics>()
    }

    return remember(context, analytics) {
        { text ->
            val activityInfo = context.googleTranslateActivityInfo()
            if (activityInfo != null) {
                context.openGoogleTranslate(
                    activity = activityInfo,
                    text = text,
                )
                analytics?.comments?.logCommentTranslate(
                    characters = text.codePointCount(0, text.length),
                )
            }
        }
    }
}

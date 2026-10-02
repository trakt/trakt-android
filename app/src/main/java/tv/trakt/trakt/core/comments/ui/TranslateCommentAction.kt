package tv.trakt.trakt.core.comments.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import org.koin.compose.koinInject
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.extensions.googleTranslateActivityInfo
import tv.trakt.trakt.common.helpers.extensions.openGoogleTranslate
import tv.trakt.trakt.common.model.Comment

@Composable
internal fun rememberTranslateCommentAction(): (Comment) -> Unit {
    val context = LocalContext.current
    val analytics = when {
        LocalInspectionMode.current -> null
        else -> koinInject<Analytics>()
    }

    return remember(context, analytics) {
        { comment ->
            val text = comment.comment.trim()
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

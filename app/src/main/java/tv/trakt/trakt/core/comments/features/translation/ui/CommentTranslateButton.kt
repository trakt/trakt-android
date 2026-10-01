package tv.trakt.trakt.core.comments.features.translation.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentMapOf
import tv.trakt.trakt.common.helpers.extensions.googleTranslateActivityInfo
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.openGoogleTranslate
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslation.Translated
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslation.Translating
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslations
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import java.util.Locale

/**
 * Translates the comment in place on device when possible, otherwise opens Google Translate.
 * Tapping again while a translation is shown restores the original text.
 */
@Composable
internal fun CommentTranslateButton(
    comment: Comment,
    translations: CommentTranslations,
    modifier: Modifier = Modifier,
    onTranslateClick: ((Comment) -> Unit)? = null,
) {
    val onDevice = translations.onDevice && onTranslateClick != null
    if (!comment.rememberTranslatable(onDeviceAvailable = onDevice)) {
        return
    }

    val context = LocalContext.current
    val translation = translations.items[comment.id]

    Icon(
        painter = painterResource(R.drawable.ic_translate),
        contentDescription = null,
        tint = when (translation) {
            is Translated -> TraktTheme.colors.accent
            Translating -> TraktTheme.colors.textPrimary.copy(alpha = 0.4F)
            null -> TraktTheme.colors.textPrimary
        },
        modifier = modifier
            .size(18.dp)
            .onClick(enabled = translation != Translating) {
                if (onDevice) {
                    onTranslateClick?.invoke(comment)
                } else {
                    context.openExternalTranslation(comment.comment.trim())
                }
            },
    )
}

internal fun Context.openExternalTranslation(text: String) {
    val activityInfo = googleTranslateActivityInfo() ?: return
    openGoogleTranslate(
        activity = activityInfo,
        text = text,
    )
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        val comment = PreviewData.comment1.copy(language = Locale.SIMPLIFIED_CHINESE)
        Row(horizontalArrangement = spacedBy(20.dp)) {
            CommentTranslateButton(
                comment = comment,
                translations = CommentTranslations(onDevice = true),
                onTranslateClick = {},
            )
            CommentTranslateButton(
                comment = comment,
                translations = CommentTranslations(
                    onDevice = true,
                    items = persistentMapOf(comment.id to Translating),
                ),
                onTranslateClick = {},
            )
            CommentTranslateButton(
                comment = comment,
                translations = CommentTranslations(
                    onDevice = true,
                    items = persistentMapOf(comment.id to Translated("Translated text")),
                ),
                onTranslateClick = {},
            )
        }
    }
}

package tv.trakt.trakt.core.comments.features.translation.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentMapOf
import tv.trakt.trakt.common.helpers.extensions.googleTranslateActivityInfo
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.openGoogleTranslate
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslation.Downloading
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslation.Translated
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslation.Translating
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslations
import tv.trakt.trakt.core.comments.model.languageFlag
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import java.util.Locale

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

    val inProgress = translation == Downloading || translation == Translating
    val originalLanguageLabel = remember(comment.language) {
        comment.language?.let { it.languageFlag() ?: it.language.uppercase(Locale.ROOT) }.orEmpty()
    }

    Row(
        horizontalArrangement = spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .onClick(enabled = !inProgress) {
                if (onDevice) {
                    onTranslateClick.invoke(comment)
                } else {
                    context.openExternalTranslation(comment.comment.trim())
                }
            },
    ) {
        if (translation == Downloading) {
            FilmProgressIndicator(
                size = 16.dp,
                color = TraktTheme.colors.textPrimary.copy(alpha = 0.4F),
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_translate),
                contentDescription = null,
                tint = when (translation) {
                    is Translated -> TraktTheme.colors.textPrimary
                    Downloading, Translating -> TraktTheme.colors.textPrimary.copy(alpha = 0.4F)
                    null -> TraktTheme.colors.textPrimary
                },
                modifier = Modifier.size(18.dp),
            )
        }

        when (translation) {
            Downloading -> {
                Text(
                    text = stringResource(R.string.text_comment_translation_downloading),
                    style = TraktTheme.typography.meta,
                    color = TraktTheme.colors.textSecondary,
                    maxLines = 1,
                )
            }
            is Translated -> {
                Text(
                    text = stringResource(
                        R.string.button_text_comment_show_original,
                        originalLanguageLabel,
                    ),
                    style = TraktTheme.typography.meta,
                    color = TraktTheme.colors.textPrimary,
                    maxLines = 1,
                )
            }
            Translating, null -> {
                Unit
            }
        }
    }
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
    TraktThemeLightDark {
        val comment = PreviewData.comment1.copy(language = Locale.SIMPLIFIED_CHINESE)
        Row(
            horizontalArrangement = spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CommentTranslateButton(
                comment = comment,
                translations = CommentTranslations(onDevice = true),
                onTranslateClick = {},
            )
            CommentTranslateButton(
                comment = comment,
                translations = CommentTranslations(
                    onDevice = true,
                    items = persistentMapOf(comment.id to Downloading),
                ),
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

package tv.trakt.trakt.core.comments.features.translation.ui

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.persistentMapOf
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslation.Downloading
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslation.Translated
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslation.Translating
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslationDownload
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslations
import tv.trakt.trakt.core.comments.features.translation.model.OnDeviceLanguages
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
    val onDevice = translations.supportsOnDevice(comment) && onTranslateClick != null
    if (!comment.rememberTranslatable(onDeviceAvailable = onDevice)) {
        return
    }

    val openExternalTranslation = rememberExternalTranslation()
    val translation = translations.translation(comment)

    val inProgress = translation is Downloading || translation == Translating
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
                    openExternalTranslation(comment.comment.trim())
                }
            },
    ) {
        if (translation is Downloading) {
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
                    is Downloading, Translating -> TraktTheme.colors.textPrimary.copy(alpha = 0.4F)
                    null -> TraktTheme.colors.textPrimary
                },
                modifier = Modifier.size(18.dp),
            )
        }

        when (translation) {
            is Downloading -> {
                Text(
                    text = downloadingLabel(translation),
                    style = TraktTheme.typography.meta,
                    color = TraktTheme.colors.textSecondary,
                    maxLines = 1,
                )
            }
            is Translated -> {
                Row(
                    horizontalArrangement = spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.button_text_comment_translated),
                        style = TraktTheme.typography.meta,
                        color = TraktTheme.colors.textPrimary,
                        maxLines = 1,
                    )
                    Text(
                        text = originalLanguageLabel,
                        style = TraktTheme.typography.meta.copy(
                            fontSize = 13.sp,
                        ),
                        color = TraktTheme.colors.textPrimary,
                        maxLines = 1,
                    )
                }
            }
            Translating, null -> {
                Unit
            }
        }
    }
}

@Composable
private fun downloadingLabel(translation: Downloading): String {
    val progress = translation.progress
    return when (translation.type) {
        CommentTranslationDownload.Language -> stringResource(R.string.text_comment_translation_downloading)
        CommentTranslationDownload.AiModel -> when (progress) {
            null -> stringResource(R.string.text_comment_translation_downloading_ai)
            else -> stringResource(R.string.text_comment_translation_downloading_ai_progress, progress)
        }
    }
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
                translations = CommentTranslations(languages = OnDeviceLanguages.All),
                onTranslateClick = {},
            )
            CommentTranslateButton(
                comment = comment,
                translations = CommentTranslations(
                    languages = OnDeviceLanguages.All,
                    items = persistentMapOf(comment.id to Downloading(CommentTranslationDownload.Language)),
                ),
                onTranslateClick = {},
            )
            CommentTranslateButton(
                comment = comment,
                translations = CommentTranslations(
                    languages = OnDeviceLanguages.All,
                    items = persistentMapOf(
                        comment.id to Downloading(CommentTranslationDownload.AiModel, progress = 42),
                    ),
                ),
                onTranslateClick = {},
            )
            CommentTranslateButton(
                comment = comment,
                translations = CommentTranslations(
                    languages = OnDeviceLanguages.All,
                    items = persistentMapOf(comment.id to Translating),
                ),
                onTranslateClick = {},
            )
            CommentTranslateButton(
                comment = comment,
                translations = CommentTranslations(
                    languages = OnDeviceLanguages.All,
                    items = persistentMapOf(
                        comment.id to Translated(
                            text = "Translated text",
                            source = comment.commentNoSpoilers,
                        ),
                    ),
                ),
                onTranslateClick = {},
            )
        }
    }
}

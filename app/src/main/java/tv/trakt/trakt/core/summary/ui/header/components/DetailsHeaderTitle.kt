package tv.trakt.trakt.core.summary.ui.header.components

import android.content.ClipData
import android.os.Build
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import tv.trakt.trakt.LocalSnackbarState
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

/** Shows [translation] when present, otherwise [title]. Tapping copies the shown title. */
@Composable
internal fun DetailsHeaderTitle(
    title: String,
    translation: String?,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboard.current
    val snackbar = LocalSnackbarState.current
    val scope = rememberCoroutineScope()
    val copiedLabel = stringResource(R.string.button_label_copied)

    Crossfade(
        targetState = translation,
        animationSpec = tween(250),
        modifier = modifier.fillMaxWidth(),
    ) { shownTranslation ->
        val displayedTitle = when {
            !shownTranslation.isNullOrBlank() -> shownTranslation
            else -> title
        }
        val titleStyle = TraktTheme.typography.heading3.copy(fontSize = 22.sp)

        Text(
            text = displayedTitle,
            textAlign = TextAlign.Center,
            color = TraktTheme.colors.textPrimary,
            style = titleStyle,
            maxLines = 1,
            overflow = Ellipsis,
            autoSize = TextAutoSize.StepBased(
                maxFontSize = titleStyle.fontSize,
                minFontSize = 16.sp,
                stepSize = 2.sp,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onClick {
                    scope.launch {
                        clipboard.setClipEntry(
                            ClipEntry(ClipData.newPlainText(displayedTitle, displayedTitle)),
                        )
                        // Android 13+ shows its own clipboard confirmation overlay.
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                            snackbar.showSnackbar(
                                message = copiedLabel,
                                duration = SnackbarDuration.Short,
                            )
                        }
                    }
                },
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        DetailsHeaderTitle(
            title = "Movie Title",
            translation = null,
        )
    }
}

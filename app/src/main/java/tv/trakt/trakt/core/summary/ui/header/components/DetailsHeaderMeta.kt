package tv.trakt.trakt.core.summary.ui.header.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.model.MediaGenre
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/** Date, length, certification and genres on one line. [episodesCount] wins over [runtime]. */
@Composable
internal fun DetailsHeaderMeta(
    genres: ImmutableList<MediaGenre>,
    modifier: Modifier = Modifier,
    date: @Composable (() -> Unit)? = null,
    episodesCount: Int? = null,
    runtime: Duration? = null,
    certification: String? = null,
    onInfoClick: (() -> Unit)? = null,
) {
    val genresText = genres.take(2)
        .map { stringResource(it.displayStringRes) }
        .joinToString(", ")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(top = 5.dp)
            .onClick { onInfoClick?.invoke() },
    ) {
        date?.let {
            it()
            DotSeparator()
        }

        if (episodesCount != null && episodesCount > 0) {
            MetaText(stringResource(R.string.tag_text_number_of_episodes, episodesCount))
            DotSeparator()
        } else if (runtime != null) {
            MetaText(rememberDurationFormat(runtime.inWholeMinutes))
            DotSeparator()
        }

        if (!certification.isNullOrBlank()) {
            MetaText(certification)
            DotSeparator()
        }

        MetaText(genresText)

        if (onInfoClick != null) {
            Spacer(Modifier.width(5.5.dp))
            Icon(
                painter = painterResource(R.drawable.ic_info),
                tint = TraktTheme.colors.textPrimary,
                contentDescription = null,
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer {
                        translationY = 0.4.dp.toPx()
                    },
            )
        }
    }
}

@Composable
private fun MetaText(text: String) {
    Text(
        text = text,
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.paragraphSmaller,
        maxLines = 1,
        overflow = Ellipsis,
    )
}

@Composable
private fun DotSeparator() {
    Text(
        text = "  •  ",
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.paragraphSmaller,
        modifier = Modifier.padding(horizontal = 1.dp),
    )
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        DetailsHeaderMeta(
            genres = persistentListOf(MediaGenre.Action, MediaGenre.Comedy),
            date = { DetailsHeaderDate(text = "2024", isReleased = true) },
            runtime = 118.minutes,
            certification = "PG-13",
            onInfoClick = {},
        )
    }
}

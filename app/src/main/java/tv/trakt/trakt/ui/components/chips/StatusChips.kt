package tv.trakt.trakt.ui.components.chips

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.model.EpisodeStatus
import tv.trakt.trakt.common.ui.theme.colors.Blue500
import tv.trakt.trakt.common.ui.theme.colors.Green600
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun EpisodeStatusChip(
    status: EpisodeStatus,
    modifier: Modifier = Modifier,
    contentTextStyle: TextStyle = TraktTheme.typography.meta,
    containerColor: Color = TraktTheme.colors.chipContainerOnContent,
) {
    val (textRes, dotColor) = when (status) {
        EpisodeStatus.Premiere -> R.string.tag_text_premiere to Green600
        EpisodeStatus.Finale -> R.string.tag_text_finale to Red500
        EpisodeStatus.New -> R.string.tag_text_new to Blue500
        EpisodeStatus.NewPremiere -> R.string.tag_text_new_premiere to Green600
        EpisodeStatus.NewFinale -> R.string.tag_text_new_finale to Red500
    }

    StatusChip(
        text = stringResource(textRes),
        dotColor = dotColor,
        contentTextStyle = contentTextStyle,
        containerColor = containerColor,
        modifier = modifier,
    )
}

@Composable
internal fun NewChip(
    modifier: Modifier = Modifier,
    contentTextStyle: TextStyle = TraktTheme.typography.meta,
    containerColor: Color = TraktTheme.colors.chipContainerOnContent,
) {
    EpisodeStatusChip(
        status = EpisodeStatus.New,
        contentTextStyle = contentTextStyle,
        containerColor = containerColor,
        modifier = modifier,
    )
}

@Composable
private fun StatusChip(
    text: String,
    modifier: Modifier = Modifier,
    dotColor: Color = TraktTheme.colors.chipContent,
    containerColor: Color = TraktTheme.colors.chipContainerOnContent,
    contentTextStyle: TextStyle = TraktTheme.typography.meta,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.spacedBy(3.5.dp),
        modifier = modifier
            .background(
                shape = RoundedCornerShape(100),
                color = containerColor,
            )
            .padding(start = 6.dp, end = 6.dp)
            .padding(
                vertical = 4.dp,
            ),
    ) {
        Box(
            modifier = Modifier
                .size(7.5.dp)
                .background(
                    color = dotColor,
                    shape = RoundedCornerShape(100),
                ),
        )
        Text(
            text = text,
            style = contentTextStyle,
            color = TraktTheme.colors.textPrimaryOnAccent,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.graphicsLayer {
                translationY = -0.5.dp.toPx()
            },
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        Column(
            verticalArrangement = spacedBy(8.dp),
        ) {
            EpisodeStatus.entries.forEach {
                EpisodeStatusChip(
                    status = it,
                    modifier = Modifier.height(20.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun CompactPreview() {
    TraktTheme {
        Column(
            verticalArrangement = spacedBy(8.dp),
        ) {
            EpisodeStatus.entries.forEach {
                EpisodeStatusChip(
                    status = it,
                    contentTextStyle = TraktTheme.typography.meta.copy(
                        fontSize = 10.sp,
                    ),
                    containerColor = TraktTheme.colors.chipContainer,
                    modifier = Modifier.height(20.dp),
                )
            }
        }
    }
}

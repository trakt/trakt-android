@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.summary.shows.features.context.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.annotation.ExperimentalCoilApi
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.ui.theme.colors.Shade910
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.components.confirmation.RemoveConfirmationSheet
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun ShowDetailsHistoryView(
    item: HomeActivityItem.EpisodeItem,
    modifier: Modifier = Modifier,
    onRemovePlayClick: (() -> Unit)? = null,
) {
    var confirmRemoveSheet by remember { mutableStateOf(false) }

    ShowDetailsHistoryContent(
        show = item.show,
        episode = item.episode,
        onRemoveWatchedClick = {
            confirmRemoveSheet = true
        },
        modifier = modifier,
    )

    RemoveConfirmationSheet(
        active = confirmRemoveSheet,
        onYes = {
            confirmRemoveSheet = false
            onRemovePlayClick?.invoke()
        },
        onNo = { confirmRemoveSheet = false },
        title = stringResource(R.string.button_text_remove_from_history),
        message = stringResource(
            R.string.warning_prompt_remove_single_watched,
            "${item.show.title} ${item.episode.seasonEpisodeString()}",
        ),
    )
}

@Composable
private fun ShowDetailsHistoryContent(
    show: Show,
    episode: Episode,
    modifier: Modifier = Modifier,
    onRemoveWatchedClick: (() -> Unit)? = null,
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Text(
            text = show.title,
            color = TraktTheme.colors.textPrimary,
            style = TraktTheme.typography.heading2,
            maxLines = 1,
            overflow = Ellipsis,
            autoSize = TextAutoSize.StepBased(
                maxFontSize = TraktTheme.typography.heading2.fontSize,
                minFontSize = 16.sp,
                stepSize = 2.sp,
            ),
        )

        Text(
            text = episode.seasonEpisodeString(),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.paragraphSmaller,
            maxLines = 1,
            overflow = Ellipsis,
            modifier = Modifier
                .padding(top = 4.dp),
        )

        Spacer(
            modifier = Modifier
                .padding(top = 22.dp)
                .background(Shade910)
                .fillMaxWidth()
                .height(1.dp),
        )

        ActionButtons(
            onRemoveWatchedClick = onRemoveWatchedClick,
            modifier = Modifier
                .padding(top = 12.dp),
        )
    }
}

@Composable
private fun ActionButtons(
    modifier: Modifier = Modifier,
    onRemoveWatchedClick: (() -> Unit)? = null,
) {
    Column(
        verticalArrangement = spacedBy(TraktTheme.spacing.contextItemsSpace),
        modifier = modifier
            .graphicsLayer {
                translationX = -9.dp.toPx()
            },
    ) {
        GhostButton(
            text = stringResource(R.string.button_text_remove_from_history),
            onClick = onRemoveWatchedClick ?: {},
            icon = painterResource(R.drawable.ic_close),
            iconSize = 18.dp,
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        ShowDetailsHistoryContent(
            show = PreviewData.show1,
            episode = PreviewData.episode1,
        )
    }
}

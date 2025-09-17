package tv.trakt.trakt.core.home.sections.activity.views.context.alt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.common.ui.theme.colors.Shade910
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem.EpisodeItem
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem.MovieItem
import tv.trakt.trakt.helpers.preview.PreviewData
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.components.buttons.GhostButton
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.Instant

@Composable
internal fun ActivityItemContextView2(
    item: HomeActivityItem,
    modifier: Modifier = Modifier,
    onRemoveWatchedClick: () -> Unit,
) {
    ActivityItemContextViewContent(
        item = item,
        modifier = modifier,
        onRemoveWatchedClick = onRemoveWatchedClick,
    )
}

@Composable
private fun ActivityItemContextViewContent(
    item: HomeActivityItem,
    modifier: Modifier = Modifier,
    onRemoveWatchedClick: () -> Unit = {},
) {
    Column(
        verticalArrangement = spacedBy(0.dp),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(12.dp),
        ) {
            TraktHeader(
                title = item.title,
                subtitle = when (item) {
                    is EpisodeItem -> item.episode.seasonEpisodeString()
                    is MovieItem -> stringResource(R.string.translated_value_type_movie)
                },
            )
        }

        Spacer(
            modifier = Modifier
                .padding(top = 16.dp, bottom = 8.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(Shade910),
        )

        GhostButton(
            text = stringResource(R.string.button_text_remove_from_history),
            icon = painterResource(R.drawable.ic_trash),
            onClick = onRemoveWatchedClick,
            modifier = Modifier
                .padding(top = 2.dp)
                .graphicsLayer {
                    translationX = -6.dp.toPx()
                },
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            ActivityItemContextViewContent(
                item = EpisodeItem(
                    id = 1L,
                    user = PreviewData.user1,
                    activity = "watched",
                    activityAt = Instant.now(),
                    episode = PreviewData.episode1,
                    show = PreviewData.show1,
                ),
            )
        }
    }
}

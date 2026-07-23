package tv.trakt.trakt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.DevicePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.skeletons.EpisodeSkeletonCard
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme

internal val EmptyVerticalSingleHeight = 219.dp
internal val EmptyVerticalDoubleHeight = 233.dp
internal val EmptyHorizontalDoubleHeight = 150.dp

internal val EmptyVerticalPanelHeight = 139.dp

@Composable
internal fun EmptyListCard(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.list_placeholder_empty),
    height: Dp = 86.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                TraktTheme.colors.dialogContainer,
                DefaultCardShape,
            )
            .padding(18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.heading6,
        )
    }
}

@DevicePreview
@Composable
private fun Preview1() {
    TraktTheme {
        Box {
            EmptyListCard(
                modifier = Modifier.padding(16.dp),
                height = EmptyVerticalSingleHeight,
            )
        }
    }
}

@DevicePreview
@Composable
private fun Preview2() {
    TraktTheme {
        EmptyListCard(
            modifier = Modifier.padding(16.dp),
            height = EmptyVerticalDoubleHeight,
        )
    }
}

@DevicePreview
@Composable
private fun Preview3() {
    TraktTheme {
        Box {
            EmptyListCard(
                modifier = Modifier
                    .padding(16.dp)
                    .alpha(0.5F),
                height = EmptyHorizontalDoubleHeight,
            )

            EpisodeSkeletonCard(
                modifier = Modifier
                    .padding(16.dp),
            )
        }
    }
}

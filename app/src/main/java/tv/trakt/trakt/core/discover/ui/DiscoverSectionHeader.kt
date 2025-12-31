package tv.trakt.trakt.core.discover.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktHeader
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun DiscoverSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    collapsed: Boolean = false,
    onCollapseClick: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(4.dp),
        ) {
            TraktHeader(
                title = title,
            )

            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .size(18.dp),
            )
        }

        Icon(
            painter = painterResource(R.drawable.ic_arrow_dropdown),
            contentDescription = null,
            tint = TraktTheme.colors.textSecondary,
            modifier = Modifier
                .padding(start = 4.dp)
                .rotate(if (collapsed) -180F else 0F)
                .size(16.dp)
                .graphicsLayer {
                    translationX = 1.dp.toPx()
                }
                .onClick(onClick = onCollapseClick),
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        DiscoverSectionHeader(
            title = "Trending Movies",
            collapsed = false,
        )
    }
}

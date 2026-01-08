package tv.trakt.trakt.ui.components

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun TraktSectionHeader(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    chevron: Boolean = true,
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
            horizontalArrangement = spacedBy(6.dp),
        ) {
            TraktHeader(
                title = title,
                subtitle = when {
                    !collapsed && subtitle != null -> subtitle
                    else -> null
                },
            )

            if (!collapsed && chevron) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Icon(
            painter = painterResource(R.drawable.ic_arrow_dropdown),
            contentDescription = null,
            tint = TraktTheme.colors.textSecondary,
            modifier = Modifier
                .padding(start = 4.dp)
                .rotate(if (collapsed) -90F else 0F)
                .size(16.dp)
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
        TraktSectionHeader(
            title = "Trending Movies",
            collapsed = false,
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        TraktSectionHeader(
            title = "Trending Movies",
            subtitle = "Subtitle Lorem Ipsum",
            collapsed = false,
        )
    }
}

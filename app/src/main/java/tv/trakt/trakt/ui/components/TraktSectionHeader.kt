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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun TraktSectionHeader(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    maxSubtitleLength: Int = Int.MAX_VALUE,
    chevron: Boolean = true,
    extraIcon: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = spacedBy(7.dp),
            modifier = Modifier.weight(1F, false),
        ) {
            TraktHeader(
                title = title,
                subtitle = subtitle,
                maxSubtitleLength = maxSubtitleLength,
                modifier = Modifier.weight(1F, false),
            )

            if (extraIcon != null) {
                extraIcon()
            }

            if (chevron) {
                Icon(
                    painter = painterResource(R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
    locale = "us",
)
@Composable
private fun Preview() {
    TraktTheme {
        TraktSectionHeader(
            title = "Trending Movies",
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
    locale = "us",
)
@Composable
private fun Preview2() {
    TraktTheme {
        TraktSectionHeader(
            title = "Trending Movies",
            subtitle = "Subtitle Lorem Ipsum",
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
    locale = "us",
)
@Composable
private fun Preview3() {
    TraktTheme {
        TraktSectionHeader(
            title = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec suscipit auctor dui.",
            subtitle = "Subtitle Lorem Ipsum",
            extraIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_more_vertical),
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier
                        .padding(start = 10.dp, end = 6.dp)
                        .size(14.dp),
                )
            },
        )
    }
}

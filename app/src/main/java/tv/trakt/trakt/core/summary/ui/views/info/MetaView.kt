package tv.trakt.trakt.core.summary.ui.views.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.thousandsFormat
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MetaView(
    plays: Int,
    watchers: Int,
    lists: Int,
    favorites: Int?,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        MetaViewItemView(
            title = plays.thousandsFormat(),
            subtitle = stringResource(R.string.text_stats_plays),
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_play),
                    contentDescription = null,
                    tint = TraktTheme.colors.textSecondary,
                    modifier = Modifier
                        .size(14.dp)
                        .graphicsLayer {
                            translationX = -1.5.dp.toPx()
                        },
                )
            },
            loading = loading,
            modifier = Modifier
                .weight(1F),
        )

        MetaViewItemView(
            title = watchers.thousandsFormat(),
            subtitle = stringResource(R.string.text_stats_watchers),
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_person_double),
                    contentDescription = null,
                    tint = TraktTheme.colors.textSecondary,
                    modifier = Modifier.size(13.dp),
                )
            },
            loading = loading,
            modifier = Modifier
                .weight(1F),
        )

        MetaViewItemView(
            title = lists.thousandsFormat(),
            subtitle = stringResource(R.string.text_stats_lists),
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_lists_off),
                    contentDescription = null,
                    tint = TraktTheme.colors.textSecondary,
                    modifier = Modifier.size(14.dp),
                )
            },
            loading = loading,
            modifier = Modifier
                .weight(1F),
        )

        favorites?.let {
            MetaViewItemView(
                title = favorites.thousandsFormat(),
                subtitle = stringResource(R.string.text_stats_favorites),
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_heart_off),
                        contentDescription = null,
                        tint = TraktTheme.colors.textSecondary,
                        modifier = Modifier.size(14.dp),
                    )
                },
                loading = loading,
                modifier = Modifier
                    .weight(1F),
            )
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun Preview() {
    TraktTheme {
        MetaView(
            plays = 12345,
            watchers = 6789,
            lists = 321,
            favorites = 987,
            loading = false,
        )
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF212427,
)
@Composable
private fun PreviewLoading() {
    TraktTheme {
        MetaView(
            plays = 0,
            watchers = 0,
            lists = 0,
            favorites = 0,
            loading = true,
        )
    }
}

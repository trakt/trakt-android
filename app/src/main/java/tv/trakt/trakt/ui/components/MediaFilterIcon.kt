package tv.trakt.trakt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun MediaFilterIcon(
    modifier: Modifier = Modifier,
    active: Boolean,
    enabled: Boolean = true,
    size: Dp = 22.dp,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .graphicsLayer {
                translationX = 1.5.dp.toPx()
            }
            .size(size)
            .onClick(
                onClick = onClick,
                enabled = enabled,
            ),
    ) {
        Icon(
            painter = painterResource(
                when (active) {
                    true -> R.drawable.ic_filter_on
                    false -> R.drawable.ic_filter_off
                },
            ),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier
                .size(size),
        )

        if (active) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .graphicsLayer {
                        translationX = -1.5.dp.toPx()
                        translationY = -1.5.dp.toPx()
                    }
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(TraktTheme.colors.accent),
            )
        }
    }
}

@Preview
@Composable
private fun MediaFilterIconPreview() {
    TraktTheme {
        Row(modifier = Modifier.padding(16.dp)) {
            MediaFilterIcon(active = false, onClick = {})
            MediaFilterIcon(
                active = true,
                modifier = Modifier.padding(start = 16.dp),
                onClick = {},
            )
        }
    }
}

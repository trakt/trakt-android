package tv.trakt.trakt.app.core.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.resources.R

@Composable
internal fun PosterChip(
    text: String,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
) {
    val shape = RoundedCornerShape(100)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(4.dp),
        modifier = modifier
            .clip(shape)
            .background(TraktTheme.colors.tagChipContainer)
            .padding(
                horizontal = 8.dp,
                vertical = 4.dp,
            ),
    ) {
        if (text.isNotBlank()) {
            Text(
                text = text.uppercase(),
                color = TraktTheme.colors.tagChipContent,
                style = TraktTheme.typography.meta,
            )
        }

        icon?.let {
            Icon(
                painter = it,
                tint = TraktTheme.colors.tagChipContent,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        PosterChip(
            text = "Watched • 2",
            icon = painterResource(R.drawable.ic_check_double),
        )
    }
}

package tv.trakt.trakt.app.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.resources.R

@Composable
internal fun InfoChip(
    text: String,
    modifier: Modifier = Modifier,
    iconVector: ImageVector? = null,
    iconPainter: Painter? = null,
    containerColor: Color = TraktTheme.colors.chipContainer,
) {
    val hasIcon = (iconVector != null || iconPainter != null)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy((2.5).dp),
        modifier = modifier
            .background(
                shape = RoundedCornerShape(100),
                color = containerColor,
            )
            .padding(
                horizontal = if (hasIcon) 5.dp else 8.dp,
                vertical = 4.dp,
            ),
    ) {
        if (iconVector != null) {
            Icon(
                imageVector = iconVector,
                contentDescription = text,
                tint = TraktTheme.colors.chipContent,
                modifier = Modifier.size(12.dp),
            )
        }
        if (iconPainter != null) {
            Icon(
                painter = iconPainter,
                contentDescription = text,
                tint = TraktTheme.colors.chipContent,
                modifier = Modifier.size(12.dp),
            )
        }
        Text(
            text = text,
            style = TraktTheme.typography.meta,
            color = TraktTheme.colors.chipContent,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun ShowCardChipPreview() {
    TraktTheme {
        InfoChip(
            text = "12K",
            iconVector = Icons.Rounded.Person,
        )
    }
}

@Preview
@Composable
private fun ShowCardChipPainterPreview() {
    TraktTheme {
        InfoChip(
            text = "12K",
            iconPainter = painterResource(R.drawable.ic_clock),
        )
    }
}

@Preview
@Composable
private fun ShowCardChipPainterNullPreview() {
    TraktTheme {
        InfoChip(
            text = "12K",
            iconPainter = null,
        )
    }
}

@Preview
@Composable
private fun ShowCardChipNullPreview() {
    TraktTheme {
        InfoChip(
            text = "12K",
            iconVector = null,
        )
    }
}

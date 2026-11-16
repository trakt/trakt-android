package tv.trakt.trakt.app.common.ui.chips

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.resources.R

@Composable
internal fun FilterChip(
    text: String,
    icon: Painter,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val contentColor = when {
        selected -> TraktTheme.colors.textPrimaryOnAccent
        else -> TraktTheme.colors.textPrimary
    }
    val containerColor = when {
        selected -> TraktTheme.colors.accent
        else -> Color.Transparent
    }
    val outlineColor = when {
        selected -> TraktTheme.colors.accent
        else -> TraktTheme.colors.chipContainer
    }

    val restingBorder = Border(
        border = BorderStroke(width = 1.5.dp, color = outlineColor),
        shape = CircleShape,
    )

    Button(
        onClick = onClick,
        modifier = modifier.height(32.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        shape = ButtonDefaults.shape(shape = CircleShape),
        border = ButtonDefaults.border(
            border = restingBorder,
            focusedBorder = Border(
                border = BorderStroke(width = 2.75.dp, color = TraktTheme.colors.primaryButtonContent),
                shape = CircleShape,
            ),
            pressedBorder = restingBorder,
        ),
        colors = ButtonDefaults.colors(
            containerColor = containerColor,
            contentColor = contentColor,
            focusedContainerColor = containerColor,
            focusedContentColor = contentColor,
            pressedContainerColor = containerColor,
            pressedContentColor = contentColor,
        ),
        scale = ButtonDefaults.scale(
            focusedScale = 1F,
        ),
    ) {
        Row(
            horizontalArrangement = spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = text,
                color = contentColor,
                style = TraktTheme.typography.buttonTertiary,
                maxLines = 1,
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        Row(
            horizontalArrangement = spacedBy(8.dp),
        ) {
            FilterChip(
                text = "Media",
                icon = painterResource(R.drawable.ic_shows_movies),
                selected = true,
            )
            FilterChip(
                text = "People",
                icon = painterResource(R.drawable.ic_person_trakt),
                selected = false,
            )
        }
    }
}

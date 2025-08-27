package tv.trakt.trakt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun FilterChip(
    selected: Boolean,
    text: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(4.dp),
        modifier = modifier
            .height(28.dp)
            .border(
                width = 1.dp,
                color = if (selected) {
                    TraktTheme.colors.accent
                } else {
                    TraktTheme.colors.chipContainer
                },
                shape = CircleShape,
            )
            .background(
                shape = RoundedCornerShape(100),
                color = if (selected) {
                    TraktTheme.colors.accent
                } else {
                    Color.Transparent
                },
            )
            .padding(
                start = if (leadingIcon != null && selected) 10.dp else 13.dp,
                end = 13.dp,
            )
            .onClick(onClick),
    ) {
        if (selected) {
            leadingIcon?.invoke()
        }
        Text(
            text = text,
            style = TraktTheme.typography.buttonTertiary,
            color = TraktTheme.colors.textPrimary,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun ShowCardChipPreview() {
    TraktTheme {
        FilterChip(
            selected = false,
            text = "Filter Chip",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Done,
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            },
        )
    }
}

@Preview
@Composable
private fun ShowCardChipPainterPreview() {
    TraktTheme {
        FilterChip(
            selected = true,
            text = "Selected Chip",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Done,
                    contentDescription = null,
                    tint = TraktTheme.colors.textPrimary,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            },
        )
    }
}

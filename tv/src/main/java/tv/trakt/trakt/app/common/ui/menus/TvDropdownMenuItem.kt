package tv.trakt.trakt.app.common.ui.menus

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.resources.R

@Composable
internal fun TvDropdownMenuItem(
    modifier: Modifier = Modifier,
    text: String,
    icon: Painter? = null,
    focused: Boolean = false,
    enabled: Boolean = true,
    onFocus: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    DropdownMenuItem(
        text = {
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .border(
                        width = 3.dp,
                        color = when {
                            focused -> Color.White
                            else -> Color.Transparent
                        },
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(horizontal = 16.dp, vertical = 16.dp),
            ) {
                icon?.let {
                    Icon(
                        painter = it,
                        contentDescription = null,
                        tint = when {
                            !enabled -> TraktTheme.colors.textPrimary.copy(alpha = 0.2F)
                            focused -> TraktTheme.colors.textPrimary
                            else -> TraktTheme.colors.textSecondary
                        },
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(
                    text = text.uppercase(),
                    textAlign = TextAlign.Start,
                    style = TraktTheme.typography.buttonPrimary,
                    color = when {
                        !enabled -> TraktTheme.colors.textPrimary.copy(alpha = 0.2F)
                        focused -> TraktTheme.colors.textPrimary
                        else -> TraktTheme.colors.textSecondary
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                )
            }
        },
        colors = MenuDefaults.selectableItemColors(
            containerColor = Color.Transparent,
        ),
        contentPadding = PaddingValues.Zero,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .onFocusChanged {
                if (it.isFocused) {
                    onFocus()
                }
            },
    )
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        TvDropdownMenuItem(
            text = "Menu Item",
            icon = painterResource(id = R.drawable.ic_info),
            focused = true,
        )
    }
}

package tv.trakt.trakt.core.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.settings.SECTION_ITEM_HEIGHT_DP
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
fun SettingsValueField(
    text: String,
    value: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = { },
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(SECTION_ITEM_HEIGHT_DP.dp)
            .onClick(
                onClick = onClick,
                enabled = enabled,
            ),
    ) {
        Text(
            text = text,
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.paragraph.copy(
                fontSize = 14.sp,
            ),
        )

        if (value.isNullOrBlank()) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .size(20.dp),
            )
        } else {
            Text(
                text = value,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraph.copy(
                    fontSize = 14.sp,
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .padding(
                        start = 32.dp,
                        end = 4.dp,
                    ),
            )
        }
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
        SettingsValueField(
            text = "Username",
            value = "traktuser123",
            modifier = Modifier
                .padding(16.dp),
        )
    }
}

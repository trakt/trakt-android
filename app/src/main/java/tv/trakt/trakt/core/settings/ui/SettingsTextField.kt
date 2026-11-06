package tv.trakt.trakt.core.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.settings.SECTION_ITEM_HEIGHT_DP
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.vip.VipChip
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SettingsTextField(
    text: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: Int? = R.drawable.ic_chevron_right,
    iconSize: Dp = 20.dp,
    color: Color = TraktTheme.colors.textPrimary,
    enabled: Boolean = true,
    vipLocked: Boolean = false,
    onClick: () -> Unit = { },
    onVipClick: () -> Unit = { },
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
        Column(
            verticalArrangement = spacedBy(3.dp),
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(end = 20.dp),
        ) {
            Text(
                text = text,
                color = color,
                style = TraktTheme.typography.paragraph.copy(
                    fontSize = 14.sp,
                ),
            )

            description?.let {
                Text(
                    text = it,
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.paragraph.copy(
                        fontSize = 11.sp,
                    ),
                    maxLines = 3,
                    overflow = Ellipsis,
                )
            }
        }

        if (!vipLocked && icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = color,
                modifier = Modifier
                    .size(iconSize),
            )
        } else if (vipLocked) {
            VipChip(
                onClick = onVipClick,
            )
        }
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        SettingsSegmentedGroup {
            item { SettingsTextField(text = "Plain") }
            item {
                SettingsTextField(
                    text = "Described",
                    description = "This is a description that can be up to 3 lines long and will be truncated with an ellipsis if it exceeds that length.",
                )
            }
            item { SettingsTextField(text = "No icon", icon = null) }
            item { SettingsTextField(text = "Colored", color = TraktTheme.colors.accent) }
            item { SettingsTextField(text = "Disabled", enabled = false) }
            item { SettingsTextField(text = "VIP locked", vipLocked = true) }
        }
    }
}

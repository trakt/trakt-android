package tv.trakt.trakt.core.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.settings.SECTION_ITEM_HEIGHT_DP
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.vip.VipChip
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SettingsTextField(
    text: String,
    modifier: Modifier = Modifier,
    icon: Int? = R.drawable.ic_chevron_right,
    iconSize: Dp = 20.dp,
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
        Text(
            text = text,
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.paragraph.copy(
                fontSize = 14.sp,
            ),
        )

        if (!vipLocked && icon != null) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
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

package tv.trakt.trakt.core.settings.ui

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.settings.SECTION_ITEM_HEIGHT_DP
import tv.trakt.trakt.core.settings.model.SettingsCategory
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private const val MAX_SUMMARY_SETTINGS = 3

@Composable
internal fun SettingsCategoryField(
    category: SettingsCategory,
    modifier: Modifier = Modifier,
    vip: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit = { },
) {
    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(SECTION_ITEM_HEIGHT_DP.dp)
            .onClick(
                onClick = onClick,
                enabled = enabled,
            ),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(24.dp),
        ) {
            Icon(
                painter = painterResource(category.icon),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier.size(
                    when (category) {
                        SettingsCategory.Account -> 23.dp
                        SettingsCategory.Support -> 20.dp
                        else -> 22.dp
                    },
                ),
            )
        }

        Column(
            verticalArrangement = spacedBy(1.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = stringResource(category.title),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.paragraph.copy(
                    fontSize = 15.sp,
                ),
            )

            val settings = when {
                vip -> category.settings + category.vipSettings
                else -> category.settings
            }

            if (settings.isNotEmpty()) {
                Text(
                    text = settings
                        .take(MAX_SUMMARY_SETTINGS)
                        .map { stringResource(it) }
                        .joinToString(separator = "  •  "),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.paragraph.copy(
                        fontSize = 11.sp,
                    ),
                    maxLines = 1,
                    overflow = Ellipsis,
                )
            }
        }

        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        SettingsSegmentedGroup {
            SettingsCategory.entries.forEach {
                item { SettingsCategoryField(category = it) }
            }
        }
    }
}

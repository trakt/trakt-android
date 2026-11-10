package tv.trakt.trakt.core.settings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.helpers.preview.PreviewData
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.UserHeaderRow
import tv.trakt.trakt.ui.components.vip.VipChip
import tv.trakt.trakt.ui.theme.DefaultCardShape
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SettingsUserPanel(
    user: User,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    UserHeaderRow(
        user = user,
        avatarBadge = { },
        trailing = {
            val badge = when {
                user.isDirector -> R.string.translated_value_job_director
                user.isAnyVip -> R.string.tag_text_vip
                else -> null
            }
            badge?.let {
                VipChip(
                    text = stringResource(it).uppercase(),
                    icon = painterResource(R.drawable.ic_stars),
                    onClick = { onClick?.invoke() },
                )
            }
        },
        modifier = modifier
            .shadow(
                elevation = TraktTheme.colors.shadowSmall,
                shape = DefaultCardShape,
                clip = true,
            )
            .background(TraktTheme.colors.panelCardContainer)
            .onClick(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
    )
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        SettingsUserPanel(
            user = PreviewData.user1,
        )
    }
}

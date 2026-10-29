package tv.trakt.trakt.core.profile.sections.leaderboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val PillShape = RoundedCornerShape(100)
private val PillHeight = 32.dp

@Composable
internal fun LeaderboardPill(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val containerColor = TraktTheme.colors.chipContainer.copy(alpha = 0.7F)
    val contentColor = TraktTheme.colors.chipContent

    Row(
        verticalAlignment = CenterVertically,
        horizontalArrangement = spacedBy(4.dp),
        modifier = modifier
            .height(PillHeight)
            .clip(PillShape)
            .background(containerColor, PillShape)
            .onClick(onClick = onClick)
            .padding(
                start = 9.dp,
                end = 4.dp,
            ),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_leaderboard),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(16.dp),
        )

        Text(
            text = stringResource(R.string.text_leaderboard),
            color = contentColor,
            style = TraktTheme.typography.meta,
            maxLines = 1,
        )

        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(12.dp),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        LeaderboardPill(
            modifier = Modifier.padding(16.dp),
        )
    }
}

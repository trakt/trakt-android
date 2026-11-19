package tv.trakt.trakt.core.summary.ui.header.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.model.MediaStatus
import tv.trakt.trakt.common.model.MediaStatus.Canceled
import tv.trakt.trakt.common.model.MediaStatus.Ended
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun DetailsHeaderStatus(
    status: MediaStatus,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(status.displayStringRes).uppercase(),
        color = when (status) {
            Canceled -> Red500
            Ended -> TraktTheme.colors.detailsStatus2
            else -> TraktTheme.colors.detailsStatus1
        },
        style = TraktTheme.typography.meta,
        modifier = modifier.padding(top = 5.dp),
    )
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        DetailsHeaderStatus(status = MediaStatus.Canceled)
    }
}

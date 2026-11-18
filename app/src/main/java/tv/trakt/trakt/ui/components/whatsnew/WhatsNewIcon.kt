package tv.trakt.trakt.ui.components.whatsnew

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.AttentionIcon
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun WhatsNewIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    onClick: () -> Unit,
) {
    AttentionIcon(
        painter = painterResource(R.drawable.ic_stars),
        size = size,
        contentDescription = stringResource(R.string.header_whats_new),
        modifier = modifier
            .size(size)
            .onClick(onClick = onClick),
    )
}

@Preview
@Composable
private fun WhatsNewIconPreview() {
    TraktTheme {
        WhatsNewIcon(
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

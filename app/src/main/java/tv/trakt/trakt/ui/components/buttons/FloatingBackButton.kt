package tv.trakt.trakt.ui.components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val ButtonSize = 36.dp
private val IconSize = 20.dp

internal val FloatingBackButtonTouchSize = 48.dp
internal val FloatingBackButtonInset = (FloatingBackButtonTouchSize - ButtonSize) / 2

@Composable
internal fun FloatingBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(ButtonSize)
            .background(
                color = TraktTheme.colors.backgroundPrimary.copy(alpha = 0.75F),
                shape = CircleShape,
            )
            .onClick(onClick = onClick),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_back_arrow),
            tint = TraktTheme.colors.textPrimary,
            contentDescription = null,
            modifier = Modifier.size(IconSize),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        FloatingBackButton(onClick = {})
    }
}

package tv.trakt.trakt.ui.components.headerbar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import tv.trakt.trakt.core.auth.ConfigAuth
import tv.trakt.trakt.core.main.helpers.MediaModeManager
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaModeButtons
import tv.trakt.trakt.ui.components.buttons.TertiaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun HeaderBar(
    modifier: Modifier = Modifier,
    containerColor: Color = TraktTheme.colors.navigationHeaderContainer,
    containerAlpha: Float = 0.98F,
    showLogin: Boolean = false,
    userLoading: Boolean = false,
) {
    val scope = rememberCoroutineScope()

    val mediaMode: MediaModeManager = koinInject()
    val currentMediaMode = remember { mediaMode.getMode() }

    HeaderBarContent(
        modifier = modifier,
        containerColor = containerColor,
        containerAlpha = containerAlpha,
        showLogin = showLogin,
        userLoading = userLoading,
        mediaMode = currentMediaMode,
        onMediaModeSelect = { mode ->
            scope.launch {
                mediaMode.setMode(mode)
            }
        },
    )
}

@Composable
private fun HeaderBarContent(
    modifier: Modifier = Modifier,
    containerColor: Color = TraktTheme.colors.navigationHeaderContainer,
    containerAlpha: Float = 0.98F,
    showLogin: Boolean = false,
    userLoading: Boolean = false,
    mediaMode: MediaMode,
    onMediaModeSelect: (MediaMode) -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current

    val contentHeight = 36.dp
    val headerBarHeight = WindowInsets.statusBars.asPaddingValues()
        .calculateTopPadding()
        .plus(TraktTheme.size.navigationHeaderHeight)

    val animatedContainerAlpha by animateFloatAsState(
        targetValue = containerAlpha,
        animationSpec = tween(),
    )

    Box(
        modifier = modifier
            .height(headerBarHeight)
            .clip(
                RoundedCornerShape(
                    bottomStart = 24.dp,
                    bottomEnd = 24.dp,
                ),
            )
            .background(containerColor.copy(alpha = animatedContainerAlpha)),
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(
                    start = TraktTheme.spacing.mainPageHorizontalSpace,
                    end = TraktTheme.spacing.mainPageHorizontalSpace,
                    bottom = TraktTheme.spacing.mainPageHorizontalSpace,
                )
                .sizeIn(minHeight = contentHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            MediaModeButtons(
                mode = mediaMode,
                height = contentHeight,
                onModeSelect = onMediaModeSelect,
            )

            if (showLogin) {
                Spacer(Modifier.weight(1F))
                TertiaryButton(
                    text = stringResource(R.string.button_text_login),
                    icon = painterResource(R.drawable.ic_trakt_icon),
                    height = contentHeight,
                    loading = userLoading,
                    enabled = !userLoading,
                    onClick = {
                        uriHandler.openUri(ConfigAuth.authCodeUrl)
                    },
                )
            }
        }
    }
}

@Preview(widthDp = 400)
@Composable
private fun Preview() {
    TraktTheme {
        HeaderBarContent(
            mediaMode = MediaMode.MEDIA,
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun Preview2() {
    TraktTheme {
        HeaderBarContent(
            showLogin = true,
            mediaMode = MediaMode.SHOWS,
        )
    }
}

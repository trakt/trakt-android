package tv.trakt.trakt.ui.components.headerbar

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import tv.trakt.trakt.MainActivity
import tv.trakt.trakt.core.auth.ConfigAuth
import tv.trakt.trakt.core.main.helpers.MediaModeManager
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.main.usecases.CustomThemeUseCase
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaModeButtons
import tv.trakt.trakt.ui.components.VipChip
import tv.trakt.trakt.ui.components.buttons.TertiaryButton
import tv.trakt.trakt.ui.components.switch.TraktThemeSwitch
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.model.CustomTheme

@Composable
internal fun HeaderBar(
    modifier: Modifier = Modifier,
    containerColor: Color = TraktTheme.colors.navigationHeaderContainer,
    containerAlpha: Float = 0.98F,
    showLogin: Boolean = false,
    showVip: Boolean = false,
    userLoading: Boolean = false,
    onVipClick: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val localActivity = LocalActivity.current

    val mediaMode: MediaModeManager = koinInject()
    val currentMediaMode = remember { mediaMode.getMode() }

    val customThemeConfig = remember {
        (localActivity as? MainActivity)?.customThemeConfig
    }

    HeaderBar(
        modifier = modifier,
        containerColor = containerColor,
        containerAlpha = containerAlpha,
        showLogin = showLogin,
        showVip = showVip,
        userLoading = userLoading,
        mediaMode = currentMediaMode,
        customTheme = customThemeConfig,
        onVipClick = onVipClick,
        onCustomThemeChange = {
            (localActivity as? MainActivity)?.toggleCustomTheme(it)
        },
        onMediaModeSelect = { mode ->
            scope.launch {
                mediaMode.setMode(mode)
            }
        },
    )
}

@Composable
private fun HeaderBar(
    modifier: Modifier = Modifier,
    containerColor: Color = TraktTheme.colors.navigationHeaderContainer,
    containerAlpha: Float = 0.98F,
    showLogin: Boolean = false,
    showVip: Boolean = false,
    userLoading: Boolean = false,
    customTheme: CustomThemeUseCase.CustomThemeConfig? = null,
    mediaMode: MediaMode,
    onMediaModeSelect: (MediaMode) -> Unit = {},
    onCustomThemeChange: (Boolean) -> Unit = {},
    onVipClick: () -> Unit = {},
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
            } else if (customTheme?.theme != null && customTheme.visible) {
                TraktThemeSwitch(
                    theme = customTheme.theme,
                    checked = customTheme.enabled,
                    onCheckedChange = onCustomThemeChange,
                    modifier = Modifier
                        .height(contentHeight),
                )
            } else if (showVip) {
                VipChip(
                    onClick = onVipClick,
                    modifier = Modifier
                        .height(contentHeight),
                )
            }
        }
    }
}

@Preview(widthDp = 400)
@Composable
private fun Preview() {
    TraktTheme {
        HeaderBar(
            mediaMode = MediaMode.MEDIA,
            showVip = true,
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun Preview2() {
    TraktTheme {
        HeaderBar(
            showLogin = true,
            mediaMode = MediaMode.SHOWS,
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun Preview3() {
    TraktTheme {
        HeaderBar(
            mediaMode = MediaMode.MEDIA,
            customTheme = CustomThemeUseCase.CustomThemeConfig(
                theme = CustomTheme(
                    id = "christmas25",
                    type = "christmas",
                    backgroundImageUrl = null,
                    colors = null,
                    filters = null,
                ),
                enabled = false,
                visible = true,
                overlayVisible = false,
            ),
        )
    }
}

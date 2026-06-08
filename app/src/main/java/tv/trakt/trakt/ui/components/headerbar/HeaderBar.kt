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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import tv.trakt.trakt.MainActivity
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.auth.ConfigAuth
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.main.usecases.CustomThemeUseCase
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.MediaFilterIcon
import tv.trakt.trakt.ui.components.MediaModeButtons
import tv.trakt.trakt.ui.components.buttons.TertiaryButton
import tv.trakt.trakt.ui.components.switch.TraktThemeSwitch
import tv.trakt.trakt.ui.components.vip.VipChip
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.model.CustomTheme

@Composable
internal fun HeaderBar(
    modifier: Modifier = Modifier,
    containerColor: Color = TraktTheme.colors.navigationHeaderContainer,
    containerAlpha: Float = 0.98F,
    showFilters: Boolean = false,
    showLogin: Boolean = false,
    showVip: Boolean = false,
    userLoading: Boolean = false,
    onVipClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val localActivity = LocalActivity.current

    val customThemeConfig = remember {
        (localActivity as? MainActivity)?.customThemeConfig
    }

    val filterManager: GlobalFilterManager = koinInject()
    val currentFilter = filterManager
        .observeFilter()
        .collectAsStateWithLifecycle(filterManager.getFilter())

    HeaderBar(
        modifier = modifier,
        containerColor = containerColor,
        containerAlpha = containerAlpha,
        filter = currentFilter.value,
        showLogin = showLogin,
        showVip = showVip,
        showFilters = showFilters,
        userLoading = userLoading,
        customTheme = customThemeConfig,
        onVipClick = onVipClick,
        onFilterClick = onFilterClick,
        onCustomThemeChange = {
            (localActivity as? MainActivity)?.toggleCustomTheme(it)
        },
        onMediaModeSelect = { mode ->
            scope.launch {
                filterManager.setFilter(currentFilter.value.copy(mode = mode))
            }
        },
    )
}

@Composable
private fun HeaderBar(
    modifier: Modifier = Modifier,
    containerColor: Color = TraktTheme.colors.navigationHeaderContainer,
    containerAlpha: Float = 0.98F,
    filter: GlobalFilter,
    showLogin: Boolean = false,
    showVip: Boolean = false,
    showFilters: Boolean = false,
    userLoading: Boolean = false,
    customTheme: CustomThemeUseCase.CustomThemeConfig? = null,
    onMediaModeSelect: (MediaMode) -> Unit = {},
    onCustomThemeChange: (Boolean) -> Unit = {},
    onVipClick: () -> Unit = {},
    onFilterClick: () -> Unit = {},
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
            if (!showLogin) {
                MediaModeButtons(
                    mode = filter.mode,
                    height = contentHeight,
                    onModeSelect = onMediaModeSelect,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
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
                    )
                }

                if (showFilters) {
                    MediaFilterIcon(
                        active = filter.isActive,
                        onClick = onFilterClick,
                    )
                }
            }
        }
    }
}

@Preview(widthDp = 400)
@Composable
private fun Preview() {
    TraktTheme {
        HeaderBar(
            filter = GlobalFilter.Default,
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
            filter = GlobalFilter.Default,
        )
    }
}

@Preview(widthDp = 400)
@Composable
private fun Preview3() {
    TraktTheme {
        HeaderBar(
            filter = GlobalFilter.Default.copy(mode = MediaMode.SHOWS),
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

@Preview(widthDp = 400)
@Composable
private fun Preview4() {
    TraktTheme {
        HeaderBar(
            filter = GlobalFilter.Default.copy(
                hideWatched = true,
            ),
            showVip = true,
            showFilters = true,
        )
    }
}

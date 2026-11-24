package tv.trakt.trakt.core.main.ui.maintenance

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.main.model.MaintenanceMode
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

private val VerticalPadding = 48.dp
private val MarkSize = 48.dp
private val ContentSpace = 16.dp
private val TouchTargetSize = 48.dp
private val SocialIconSize = 24.dp

@Composable
internal fun MaintenanceView(
    modifier: Modifier = Modifier,
    viewModel: MaintenanceViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    val uriHandler = LocalUriHandler.current

    BackHandler(
        enabled = state.maintenance != null,
        onBack = { activity?.finish() },
    )

    MaintenanceContent(
        maintenance = state.maintenance,
        modifier = modifier,
        onUrlClick = uriHandler::openUri,
    )
}

@Composable
private fun MaintenanceContent(
    maintenance: MaintenanceMode?,
    modifier: Modifier = Modifier,
    onUrlClick: (String) -> Unit = {},
) {
    AnimatedVisibility(
        visible = maintenance != null,
        enter = fadeIn(tween(250)),
        exit = fadeOut(tween(150)),
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = spacedBy(ContentSpace, Alignment.CenterVertically),
            modifier = Modifier
                .fillMaxSize()
                .background(TraktTheme.colors.backgroundPrimary)
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = null,
                )
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(
                    state = rememberScrollState(),
                    overscrollEffect = null,
                )
                .padding(
                    horizontal = TraktTheme.spacing.mainPageHorizontalSpace,
                    vertical = VerticalPadding,
                ),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_hourglass),
                contentDescription = null,
                tint = TraktTheme.colors.accent,
                modifier = Modifier.size(MarkSize),
            )

            Text(
                text = maintenance?.title ?: stringResource(R.string.page_title_service_unavailable),
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading4,
                textAlign = TextAlign.Center,
            )

            val message = maintenance?.message
            if (message != null) {
                MaintenanceMessage(message)
            } else {
                MaintenanceMessage(stringResource(R.string.error_text_service_unavailable))
                MaintenanceMessage(stringResource(R.string.error_text_service_unavailable_resume))
            }

            PrimaryButton(
                text = stringResource(R.string.link_text_service_status),
                onClick = { onUrlClick(Config.WEB_STATUS_URL) },
                modifier = Modifier.padding(top = ContentSpace),
            )

            Row {
                MaintenanceSocialLink(
                    icon = R.drawable.ic_reddit,
                    onClick = { onUrlClick(Config.WEB_SOCIAL_REDDIT_URL) },
                )
                MaintenanceSocialLink(
                    icon = R.drawable.ic_x_twitter,
                    onClick = { onUrlClick(Config.WEB_SOCIAL_X_URL) },
                )
            }
        }
    }
}

@Composable
private fun MaintenanceSocialLink(
    @DrawableRes icon: Int,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(TouchTargetSize)
            .onClick(onClick = onClick),
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = TraktTheme.colors.textPrimary,
            modifier = Modifier.size(SocialIconSize),
        )
    }
}

@Composable
private fun MaintenanceMessage(text: String) {
    Text(
        text = text,
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.paragraphSmall,
        textAlign = TextAlign.Center,
    )
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        MaintenanceContent(
            maintenance = MaintenanceMode(title = null, message = null),
        )
    }
}

@PreviewTraktLightDark
@Composable
private fun PreviewRemoteCopy() {
    TraktThemePreview {
        MaintenanceContent(
            maintenance = MaintenanceMode(
                title = "Scheduled maintenance",
                message = "Trakt is getting an upgrade. We will be back within the hour.",
            ),
        )
    }
}

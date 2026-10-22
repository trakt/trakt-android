package tv.trakt.trakt.core.main.ui.locked

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber
import tv.trakt.trakt.common.Config
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.networking.helpers.AccountLock
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.components.buttons.TertiaryButton
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun LockedAccountView(
    modifier: Modifier = Modifier,
    viewModel: LockedAccountViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    BackHandler(
        enabled = state.lock != null,
        onBack = viewModel::dismiss,
    )

    LockedAccountContent(
        lock = state.lock,
        modifier = modifier,
        onAdvancedSettingsClick = {
            uriHandler.openUri(Config.WEB_ADVANCED_SETTINGS_URL)
        },
        onSupportClick = {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:".toUri()
                putExtra(Intent.EXTRA_EMAIL, arrayOf(Config.WEB_SUPPORT_MAIL))
            }
            try {
                context.startActivity(intent)
            } catch (error: Exception) {
                Timber.w(error, "Unable to start email client")
            }
        },
        onCloseClick = viewModel::dismiss,
    )
}

@Composable
private fun LockedAccountContent(
    lock: AccountLock?,
    modifier: Modifier = Modifier,
    onAdvancedSettingsClick: () -> Unit = {},
    onSupportClick: () -> Unit = {},
    onCloseClick: () -> Unit = {},
) {
    AnimatedVisibility(
        visible = lock != null,
        enter = fadeIn(tween(250)),
        exit = fadeOut(tween(150)),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TraktTheme.colors.backgroundPrimary)
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = null,
                )
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(24.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = null,
                tint = TraktTheme.colors.textPrimary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
                    .onClick(onClick = onCloseClick),
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_home_off),
                    contentDescription = null,
                    tint = TraktTheme.colors.accent,
                    modifier = Modifier.size(48.dp),
                )

                Text(
                    text = stringResource(R.string.error_kicker_account_locked).uppercase(),
                    color = TraktTheme.colors.textSecondary,
                    style = TraktTheme.typography.meta,
                )

                Text(
                    text = stringResource(R.string.page_title_account_locked),
                    color = TraktTheme.colors.textPrimary,
                    style = TraktTheme.typography.heading4,
                    textAlign = TextAlign.Center,
                )

                when (lock) {
                    AccountLock.CollectionLimit -> {
                        LockedAccountMessage(R.string.error_text_locked_account_collection_limit)
                    }
                    AccountLock.UsageLimit, null -> {
                        LockedAccountMessage(R.string.error_text_locked_account_guide)
                        LockedAccountMessage(R.string.error_text_locked_account_persists)
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = spacedBy(8.dp),
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    if (lock != AccountLock.CollectionLimit) {
                        PrimaryButton(
                            text = stringResource(R.string.link_text_advanced_settings),
                            onClick = onAdvancedSettingsClick,
                        )
                    }
                    TertiaryButton(
                        text = stringResource(R.string.link_text_support),
                        onClick = onSupportClick,
                        containerColor = TraktTheme.colors.dialogContainer,
                        contentColor = TraktTheme.colors.textPrimary,
                    )
                }
            }
        }
    }
}

@Composable
private fun LockedAccountMessage(id: Int) {
    Text(
        text = stringResource(id),
        color = TraktTheme.colors.textSecondary,
        style = TraktTheme.typography.paragraphSmall,
        textAlign = TextAlign.Center,
    )
}

@Preview(device = "id:pixel_9", showBackground = true, backgroundColor = 0xFF131517)
@Composable
private fun Preview() {
    TraktTheme {
        LockedAccountContent(lock = AccountLock.UsageLimit)
    }
}

@Preview(device = "id:pixel_9", showBackground = true, backgroundColor = 0xFF131517)
@Composable
private fun PreviewCollectionLimit() {
    TraktTheme {
        LockedAccountContent(lock = AccountLock.CollectionLimit)
    }
}

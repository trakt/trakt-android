package tv.trakt.trakt.app.core.auth

import android.content.ClipData
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component3
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import com.lightspark.composeqr.DotShape
import com.lightspark.composeqr.QrCodeColors
import com.lightspark.composeqr.QrCodeView
import kotlinx.coroutines.launch
import tv.trakt.trakt.app.BuildConfig
import tv.trakt.trakt.app.LocalSnackbarState
import tv.trakt.trakt.app.R
import tv.trakt.trakt.app.common.ui.FilmProgressIndicator
import tv.trakt.trakt.app.common.ui.GenericErrorView
import tv.trakt.trakt.app.common.ui.buttons.PrimaryButton
import tv.trakt.trakt.app.core.auth.AuthState.LoadingState.LOADING
import tv.trakt.trakt.app.core.auth.AuthState.LoadingState.REJECTED
import tv.trakt.trakt.app.core.auth.AuthState.LoadingState.SUCCESS
import tv.trakt.trakt.app.core.auth.model.AuthDeviceCode
import tv.trakt.trakt.app.core.details.ui.BackdropImage
import tv.trakt.trakt.app.ui.theme.TraktTheme

@Composable
internal fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthorized: () -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val localContext = LocalContext.current
    val localSnack = LocalSnackbarState.current

    LaunchedEffect(state.loadingState) {
        if (state.loadingState == SUCCESS) {
            onAuthorized()
            localSnack.showSnackbar(localContext.getString(R.string.info_signed_in))
        }
    }

    AuthScreenContent(
        state = state,
        onRetryClick = { viewModel.loadData() },
    )
}

@Composable
private fun AuthScreenContent(
    state: AuthState,
    modifier: Modifier = Modifier,
    onRetryClick: () -> Unit = { },
) {
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current

    val (loading, content, retry) = remember { FocusRequester.createRefs() }

    LaunchedEffect(state.authDeviceCode) {
        // For ease of testing. If the device code is available, copy it to the clipboard.
        if (BuildConfig.DEBUG && state.authDeviceCode != null) {
            scope.launch {
                val userCode = state.authDeviceCode.userCode
                val clipData = ClipData.newPlainText(userCode, userCode)
                clipboard.setClipEntry(clipData.toClipEntry())
            }
        }
    }

    Box(
        contentAlignment = Alignment.TopStart,
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary),
    ) {
        BackdropImage(
            imageUrl = state.backgroundUrl,
            saturation = 0F,
            crossfade = true,
        )

        AnimatedVisibility(
            visible = state.loadingState == LOADING,
            enter = fadeIn(animationSpec = tween(delayMillis = 500)),
            modifier = Modifier.align(Alignment.Center),
        ) {
            LaunchedEffect(Unit) {
                loading.requestFocus()
            }

            FilmProgressIndicator(
                modifier = Modifier
                    .focusRequester(loading)
                    .focusable(),
            )
        }

        if (state.error == null) {
            if (state.authDeviceCode != null && state.loadingState != REJECTED) {
                DeviceCodeContent(
                    focusRequester = content,
                    authDeviceCode = { state.authDeviceCode },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(start = 48.dp),
                )
            }
            if (state.loadingState == REJECTED) {
                DeviceCodeRejected(
                    focusRequester = retry,
                    onRetryClick = onRetryClick,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(start = 16.dp),
                )
            }
        } else {
            GenericErrorView(
                error = state.error,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = TraktTheme.spacing.mainContentStartSpace,
                        end = TraktTheme.spacing.mainContentEndSpace,
                    ),
            )
        }
    }
}

@Composable
private fun DeviceCodeRejected(
    focusRequester: FocusRequester,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = spacedBy(16.dp),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.error_device_code_expired),
            color = TraktTheme.colors.textSecondary,
            style = TraktTheme.typography.heading5,
            textAlign = TextAlign.Center,
        )

        PrimaryButton(
            text = stringResource(R.string.retry),
            onClick = onRetryClick,
            modifier = Modifier
                .width(180.dp)
                .focusRequester(focusRequester),
        )
    }
}

@Composable
private fun DeviceCodeContent(
    focusRequester: FocusRequester,
    authDeviceCode: () -> AuthDeviceCode,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        horizontalArrangement = spacedBy(32.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        QrCodeView(
            data = "${authDeviceCode().url}/${authDeviceCode().userCode}",
            modifier = Modifier.size(240.dp),
            colors = QrCodeColors(
                background = TraktTheme.colors.backgroundPrimary,
                foreground = Color.White,
            ),
            dotShape = DotShape.Circle,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(TraktTheme.colors.backgroundPrimary)
                    .padding(8.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_trakt_placeholder),
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(42.dp),
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .focusRequester(focusRequester)
                .focusable(),
        ) {
            Text(
                text = stringResource(R.string.auth_sign_in_header1),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.heading5,
            )
            Text(
                text = authDeviceCode().url,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading3,
                modifier = Modifier.padding(top = 2.dp),
            )

            Text(
                text = stringResource(R.string.auth_sign_in_header2),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.heading5,
                modifier = Modifier.padding(top = 20.dp),
            )
            Text(
                text = authDeviceCode().userCode,
                color = TraktTheme.colors.textPrimary,
                style = TraktTheme.typography.heading3,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun MainScreenPreview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.LightGray.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            AuthScreenContent(
                state = AuthState(),
            )
        }
    }
}

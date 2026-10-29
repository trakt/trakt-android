package tv.trakt.trakt.core.inreview

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import timber.log.Timber
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.common.helpers.extensions.PreviewTraktLightDark
import tv.trakt.trakt.helpers.extensions.TraktThemePreview
import tv.trakt.trakt.ui.components.buttons.FloatingBackButton
import tv.trakt.trakt.ui.components.buttons.FloatingBackButtonInset
import tv.trakt.trakt.ui.theme.TraktTheme

private val BackButtonTopPadding = 46.dp

@Composable
internal fun InReviewScreen(
    viewModel: InReviewViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bottomBarVisibility = LocalBottomBarVisibility.current
    val uriHandler = LocalUriHandler.current

    DisposableEffect(Unit) {
        bottomBarVisibility.value = false
        onDispose {
            bottomBarVisibility.value = true
        }
    }

    InReviewContent(
        state = state,
        onExternalLinkClick = { url ->
            runCatching { uriHandler.openUri(url) }
                .onFailure { Timber.w(it, "No activity to open external link") }
        },
        onBackClick = onNavigateBack,
        modifier = modifier,
    )
}

@Composable
private fun InReviewContent(
    state: InReviewState,
    onExternalLinkClick: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentPadding = PaddingValues(
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding(),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding(),
    )

    val backgroundColor = TraktTheme.colors.backgroundPrimary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(contentPadding),
    ) {
        if (state.url != null && !LocalInspectionMode.current) {
            InReviewWebView(
                url = state.url,
                backgroundColor = backgroundColor,
                onExternalLinkClick = onExternalLinkClick,
                modifier = Modifier.fillMaxSize(),
            )
        }

        FloatingBackButton(
            onClick = onBackClick,
            modifier = Modifier.padding(
                start = TraktTheme.spacing.mainPageHorizontalSpace - FloatingBackButtonInset,
                top = BackButtonTopPadding,
            ),
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun InReviewWebView(
    url: String,
    backgroundColor: Color,
    onExternalLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnExternalLinkClick by rememberUpdatedState(onExternalLinkClick)

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                setBackgroundColor(backgroundColor.toArgb())
                settings.javaScriptEnabled = true
                // The web app keeps the slurm token in sessionStorage after stripping it from the URL.
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest,
                    ): Boolean {
                        if (isTraktWebUrl(request.url)) {
                            return false
                        }
                        currentOnExternalLinkClick(request.url.toString())
                        return true
                    }
                }
                loadUrl(url)
            }
        },
        onRelease = { it.destroy() },
    )
}

@PreviewTraktLightDark
@Composable
private fun Preview() {
    TraktThemePreview {
        InReviewContent(
            state = InReviewState(),
            onExternalLinkClick = {},
            onBackClick = {},
        )
    }
}

package tv.trakt.trakt.core.search.features.game

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import tv.trakt.trakt.LocalBottomBarVisibility
import tv.trakt.trakt.common.Config.WEB_GAME_RATED_HIGHER_URL
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

private val gameBackgroundColor = Color(0xFF050406)

private val BackButtonSize = 36.dp
private val BackButtonTouchSize = 48.dp
private val BackButtonInset = (BackButtonTouchSize - BackButtonSize) / 2

@Composable
internal fun GameScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bottomBarVisibility = LocalBottomBarVisibility.current

    DisposableEffect(Unit) {
        bottomBarVisibility.value = false
        onDispose {
            bottomBarVisibility.value = true
        }
    }

    GameContent(
        url = WEB_GAME_RATED_HIGHER_URL,
        onBackClick = onNavigateBack,
        modifier = modifier,
    )
}

@Composable
private fun GameContent(
    url: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentPadding = PaddingValues(
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding(),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding(),
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(gameBackgroundColor)
            .padding(contentPadding),
    ) {
        if (!LocalInspectionMode.current) {
            GameWebView(
                url = url,
                modifier = Modifier.fillMaxSize(),
            )
        }

        BackButton(
            onClick = onBackClick,
            modifier = Modifier.padding(
                start = TraktTheme.spacing.mainPageHorizontalSpace - BackButtonInset,
                top = 13.dp,
            ),
        )
    }
}

@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(BackButtonTouchSize)
            .onClick(onClick = onClick),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(BackButtonSize)
                .background(
                    color = TraktTheme.colors.backgroundPrimary.copy(alpha = 0.75F),
                    shape = CircleShape,
                ),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                tint = TraktTheme.colors.textPrimary,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun GameWebView(
    url: String,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                setBackgroundColor(gameBackgroundColor.toArgb())
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = WebViewClient()
                loadUrl(url)
            }
        },
        onRelease = { it.destroy() },
    )
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        GameContent(
            url = WEB_GAME_RATED_HIGHER_URL,
            onBackClick = {},
        )
    }
}

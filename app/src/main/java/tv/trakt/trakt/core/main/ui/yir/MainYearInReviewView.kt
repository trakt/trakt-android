package tv.trakt.trakt.core.main.ui.yir

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.ui.components.yir.YearInReviewBanner
import tv.trakt.trakt.ui.theme.TraktTheme

private const val YEAR_IN_REVIEW_YEAR = 2026

private val ScrimColor = Color.Black.copy(alpha = 0.9F)
private val BannerShape = RoundedCornerShape(20.dp)

@Composable
internal fun MainYearInReviewView(
    visible: Boolean,
    url: String?,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(350)),
        exit = fadeOut(tween(150)),
        modifier = modifier,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(ScrimColor)
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = null,
                )
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(12.dp),
        ) {
            YearInReviewBanner(
                year = YEAR_IN_REVIEW_YEAR,
                onCheckClick = {
                    url?.let(uriHandler::openUri)
                    onDismiss()
                },
                onCloseClick = onDismiss,
                modifier = Modifier
                    .animateEnterExit(
                        enter = scaleIn(tween(350), initialScale = 0.95F),
                        exit = scaleOut(tween(150), targetScale = 0.95F),
                    )
                    .clip(BannerShape),
            )
        }
    }
}

@Preview(device = "id:pixel_5")
@Composable
private fun Preview() {
    TraktTheme {
        MainYearInReviewView(
            visible = true,
            url = null,
        )
    }
}

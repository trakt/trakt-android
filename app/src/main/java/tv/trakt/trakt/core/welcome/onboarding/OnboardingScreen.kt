package tv.trakt.trakt.core.welcome.onboarding

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.core.welcome.onboarding.pages.OnboardingPage1
import tv.trakt.trakt.core.welcome.onboarding.pages.OnboardingPage2
import tv.trakt.trakt.core.welcome.onboarding.pages.OnboardingPage3
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.extensions.isAtLeastMedium
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    val windowClass = currentWindowAdaptiveInfo().windowSizeClass
    val contentVerticalPadding = PaddingValues(
        top = WindowInsets.statusBars.asPaddingValues()
            .calculateTopPadding()
            .plus(
                when {
                    windowClass.isAtLeastMedium() -> 64.dp
                    else -> 32.dp
                },
            ),
        bottom = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding()
            .plus(32.dp),
    )

    val currentPage = remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary),
    ) {
        Crossfade(
            targetState = currentPage,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentVerticalPadding),
        ) { currentPage ->
            when (currentPage.intValue) {
                0 -> OnboardingPage1(
                    modifier = Modifier.fillMaxSize(),
                )

                1 -> OnboardingPage2(
                    modifier = Modifier.fillMaxSize(),
                )

                2 -> OnboardingPage3(
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(contentVerticalPadding)
                .padding(
                    horizontal = when {
                        windowClass.isAtLeastMedium() -> 128.dp
                        else -> 24.dp
                    },
                )
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PageIndicator(
                pageCount = 3,
                currentPage = currentPage.intValue,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            PrimaryButton(
                text = stringResource(R.string.button_text_continue).uppercase(),
                containerColor = Purple500,
                contentColor = Color.White,
                onClick = {
                    currentPage.intValue = when {
                        currentPage.intValue < 2 -> {
                            currentPage.intValue + 1
                        }

                        else -> {
                            onDismiss()
                            2
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
internal fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val dotColor = animateColorAsState(
                targetValue = when {
                    isActive -> Purple500
                    else -> Color.White.copy(alpha = 0.25f)
                },
                animationSpec = tween(durationMillis = 250),
                label = "dotColor",
            )
            val dotSize = animateDpAsState(
                targetValue = if (isActive) 8.dp else 6.dp,
                animationSpec = tween(durationMillis = 250),
                label = "dotSize",
            )

            Box(
                modifier = Modifier
                    .size(dotSize.value)
                    .background(
                        color = dotColor.value,
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Preview(
    device = "id:pixel_4",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        val previewHandler = AsyncImagePreviewHandler {
            ColorImage(Color.Blue.toArgb())
        }
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            OnboardingScreen(
                onDismiss = {},
            )
        }
    }
}

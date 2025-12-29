package tv.trakt.trakt.core.welcome.onboarding

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.common.ui.theme.colors.Purple500
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
//        Box(
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .fillMaxWidth()
//                .fillMaxHeight(0.75F)
//                .background(backgroundGradient),
//        )

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
        )
    }
}

@Composable
internal fun OnboardingPage1(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Onboarding Page 1",
            style = TraktTheme.typography.heading4,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun OnboardingPage2(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Onboarding Page 2",
            style = TraktTheme.typography.heading4,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun OnboardingPage3(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Onboarding Page 3",
            style = TraktTheme.typography.heading4,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
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

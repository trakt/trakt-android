package tv.trakt.trakt.core.welcome.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush.Companion.verticalGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.common.ui.theme.colors.Shade940
import tv.trakt.trakt.core.welcome.onboarding.pages.OnboardingPage1
import tv.trakt.trakt.core.welcome.onboarding.pages.OnboardingPage2
import tv.trakt.trakt.core.welcome.onboarding.pages.OnboardingPage3
import tv.trakt.trakt.core.welcome.onboarding.pages.OnboardingPage4
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.extensions.isAtLeastMedium
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onLogin: () -> Unit,
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
    val isLastPage = remember(currentPage.intValue) {
        currentPage.intValue >= 3
    }

    val backgroundGradient = remember {
        verticalGradient(
            colors = listOf(
                Shade940.copy(alpha = 0.2F),
                Shade940.copy(alpha = 0.82F),
                Shade940.copy(alpha = 1F),
            ),
        )
    }

    val parallaxOffset = animateFloatAsState(
        targetValue = currentPage.intValue * -30f,
        animationSpec = tween(durationMillis = 500),
        label = "parallaxOffset",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraktTheme.colors.backgroundPrimary),
    ) {
        Image(
            painter = painterResource(R.drawable.img_onboarding),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = contentVerticalPadding.calculateBottomPadding() * 3)
                .graphicsLayer {
                    translationX = parallaxOffset.value
                    scaleX = 1.2f
                    scaleY = 1.2f
                },
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient),
        )

        AnimatedContent(
            targetState = currentPage.intValue,
            transitionSpec = {
                (
                    slideInHorizontally(
                        animationSpec = tween(500),
                        initialOffsetX = { fullWidth -> fullWidth / 4 },
                    ) + fadeIn(
                        animationSpec = tween(350),
                    )
                ).togetherWith(
                    slideOutHorizontally(
                        animationSpec = tween(500),
                        targetOffsetX = { fullWidth -> -(fullWidth / 4) },
                    ) + fadeOut(
                        animationSpec = tween(350),
                    ),
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    bottom = contentVerticalPadding
                        .calculateBottomPadding()
                        .plus(46.dp) // Button height
                        .plus(68.dp),
                ),
            label = "onboarding_page_transition",
        ) { currentPage ->
            val paddingHorizontal = when {
                windowClass.isAtLeastMedium() -> 128.dp
                else -> 24.dp
            }
            when (currentPage) {
                0 -> OnboardingPage1(
                    Modifier
                        .padding(horizontal = paddingHorizontal)
                        .fillMaxHeight(),
                )
                1 -> OnboardingPage2(
                    Modifier
                        .padding(horizontal = paddingHorizontal)
                        .fillMaxHeight(),
                )
                2 -> OnboardingPage3(
                    Modifier
                        .padding(horizontal = paddingHorizontal)
                        .fillMaxHeight(),
                )
                3 -> OnboardingPage4(
                    Modifier
                        .padding(horizontal = paddingHorizontal)
                        .fillMaxHeight(),
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(contentVerticalPadding)
                .padding(
                    horizontal = when {
                        windowClass.isAtLeastMedium() -> 128.dp
                        else -> 24.dp
                    },
                )
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = spacedBy(24.dp),
        ) {
            PrimaryButton(
                text = when {
                    isLastPage -> stringResource(R.string.button_text_join_trakt)
                    else -> stringResource(R.string.button_text_continue)
                },
                containerColor = Purple500,
                contentColor = Color.White,
                onClick = {
                    currentPage.intValue = when {
                        isLastPage -> {
                            onLogin()
                            3
                        }
                        else -> {
                            currentPage.intValue + 1
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier
                    .height(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PageIndicator(
                    pageCount = 4,
                    currentPage = currentPage.intValue,
                    modifier = Modifier
                        .padding(top = 1.dp),
                )

                AnimatedVisibility(
                    visible = !isLastPage,
                    enter = fadeIn(animationSpec = tween(250)),
                    exit = fadeOut(animationSpec = tween(250)),
                ) {
                    Text(
                        text = stringResource(R.string.button_text_login),
                        style = TraktTheme.typography.buttonTertiary,
                        color = TraktTheme.colors.textPrimary,
                        modifier = Modifier
                            .onClick(
                                enabled = !isLastPage,
                                onClick = onLogin,
                            ),
                    )
                }
            }
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
        modifier = modifier.height(8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val dotColor = animateColorAsState(
                targetValue = when {
                    isActive -> Purple500
                    else -> Color.White
                },
                animationSpec = tween(durationMillis = 250),
                label = "dotColor",
            )
            val dotSize = animateDpAsState(
                targetValue = if (isActive) 8.dp else 5.dp,
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
                onLogin = {},
            )
        }
    }
}

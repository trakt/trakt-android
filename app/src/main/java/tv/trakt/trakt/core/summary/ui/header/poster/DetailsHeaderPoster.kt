package tv.trakt.trakt.core.summary.ui.header.poster

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.ifOrElse
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.ImdbId
import tv.trakt.trakt.core.summary.ui.DetailsPoster
import tv.trakt.trakt.core.summary.ui.header.PosterChipsGroup
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.TraktShareButton
import tv.trakt.trakt.ui.extensions.isAtLeastLarge
import tv.trakt.trakt.ui.extensions.isAtLeastMedium
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
fun DetailsHeaderPoster(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    imagePlaceholderUrl: String?,
    accentColor: Color?,
    loading: Boolean,
    creditsCount: Int?,
    playsCount: Int?,
    watched: Boolean,
    watching: Boolean,
    personImdb: ImdbId?,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onShareImageClick: () -> Unit,
    onWatchedClick: () -> Unit,
    extraRightColumn: @Composable () -> Unit = {},
) {
    val windowClass = currentWindowAdaptiveInfo().windowSizeClass

    var animatedEnter by rememberSaveable { mutableStateOf(false) }
    val animatedAlpha = animateFloatAsState(
        targetValue = if (animatedEnter) 1F else 0F,
        animationSpec = tween(500),
    )
    val animatedTranslation by animateDpAsState(
        targetValue = if (animatedEnter) 0.dp else (2).dp,
        animationSpec = tween(500),
    )

    LaunchedEffect(Unit) {
        if (!animatedEnter) {
            animatedEnter = true
        }
    }

    Column(
        verticalArrangement = spacedBy(16.dp),
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = spacedBy(0.dp, CenterHorizontally),
            verticalAlignment = Alignment.Top,
        ) {
            val posterSpace = TraktTheme.spacing.detailsHeaderHorizontalSpace

            Column(
                modifier = Modifier
                    .width(posterSpace)
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back_arrow),
                    tint = TraktTheme.colors.textPrimary,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 20.dp)
                        .size(24.dp)
                        .onClick(onClick = onBackClick),
                )
            }

            Box(
                modifier = Modifier.weight(1F, false),
            ) {
                val posterModifier = remember(windowClass) {
                    Modifier
                        .ifOrElse(
                            windowClass.isAtLeastMedium(),
                            isTrue = when {
                                windowClass.isAtLeastLarge() -> Modifier.width(256.dp)
                                else -> Modifier.width(328.dp)
                            },
                            isFalse = Modifier,
                        )
                }

                DetailsPoster(
                    imageUrl = imageUrl,
                    imagePlaceholderUrl = imagePlaceholderUrl,
                    imageAlpha = animatedAlpha.value,
                    imageOffsetY = animatedTranslation,
                    color = accentColor,
                    modifier = posterModifier,
                )

                this@Row.AnimatedVisibility(
                    visible = !loading,
                    enter = fadeIn(tween(150)),
                    exit = fadeOut(tween(150)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .graphicsLayer {
                            translationY = 8.5.dp.toPx()
                        },
                ) {
                    PosterChipsGroup(
                        creditsCount = creditsCount ?: 0,
                        playsCount = playsCount ?: 0,
                        watched = watched,
                        watching = watching,
                        personImdb = personImdb,
                        onWatchedChipClick = onWatchedClick,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .width(posterSpace)
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                TraktShareButton(
                    onShareLinkClick = onShareClick,
                    onShareImageClick = onShareImageClick,
                    modifier = Modifier.padding(start = 20.dp),
                )

                extraRightColumn.invoke()
            }
        }
    }
}

@Preview(
    device = "id:pixel_5",
    showBackground = true,
    backgroundColor = 0xFF131517,
    locale = "us",
)
@Composable
private fun Preview() {
    TraktTheme {
        DetailsHeaderPoster(
            imageUrl = null,
            imagePlaceholderUrl = null,
            accentColor = TraktTheme.colors.accent,
            loading = false,
            creditsCount = 3,
            playsCount = 1,
            watched = false,
            watching = false,
            personImdb = null,
            onBackClick = {},
            onShareClick = {},
            onShareImageClick = {},
            onWatchedClick = {},
            modifier = Modifier
                .padding(bottom = 9.dp),
        )
    }
}

@Preview(
    device = "id:pixel_tablet",
    showBackground = true,
    backgroundColor = 0xFF131517,
    locale = "us",
)
@Composable
private fun Preview2() {
    TraktTheme {
        DetailsHeaderPoster(
            imageUrl = null,
            imagePlaceholderUrl = null,
            accentColor = TraktTheme.colors.accent,
            loading = false,
            creditsCount = 3,
            playsCount = 1,
            watched = false,
            watching = false,
            personImdb = null,
            onBackClick = {},
            onShareClick = {},
            onShareImageClick = {},
            onWatchedClick = {},
            modifier = Modifier
                .padding(bottom = 9.dp),
        )
    }
}

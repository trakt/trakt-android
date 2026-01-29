package tv.trakt.trakt.core.summary.ui.header.poster

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.remember
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
import tv.trakt.trakt.ui.extensions.isAtLeastLarge
import tv.trakt.trakt.ui.extensions.isAtLeastMedium
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
fun DetailsHeaderPoster(
    imageUrl: String?,
    accentColor: Color?,
    loading: Boolean,
    creditsCount: Int?,
    playsCount: Int?,
    personImdb: ImdbId?,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
    extraRightColumn: @Composable () -> Unit = {},
) {
    val windowClass = currentWindowAdaptiveInfo().windowSizeClass

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
                        creditsCount = creditsCount,
                        playsCount = playsCount,
                        personImdb = personImdb,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .width(posterSpace)
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.Start,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_share),
                    tint = TraktTheme.colors.textPrimary,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 20.dp)
                        .size(24.dp)
                        .onClick(onClick = onShareClick),
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
)
@Composable
private fun Preview() {
    TraktTheme {
        DetailsHeaderPoster(
            imageUrl = null,
            accentColor = TraktTheme.colors.accent,
            loading = false,
            creditsCount = 3,
            playsCount = 0,
            personImdb = null,
            onBackClick = {},
            onShareClick = {},
            modifier = Modifier
                .padding(bottom = 9.dp),
        )
    }
}

@Preview(
    device = "id:pixel_tablet",
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview2() {
    TraktTheme {
        DetailsHeaderPoster(
            imageUrl = null,
            accentColor = TraktTheme.colors.accent,
            loading = false,
            creditsCount = 3,
            playsCount = 0,
            personImdb = null,
            onBackClick = {},
            onShareClick = {},
            modifier = Modifier
                .padding(bottom = 9.dp),
        )
    }
}

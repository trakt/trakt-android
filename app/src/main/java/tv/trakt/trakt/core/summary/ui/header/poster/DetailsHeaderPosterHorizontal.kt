package tv.trakt.trakt.core.summary.ui.header.poster

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.ifOrElse
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.core.summary.ui.DetailsHorizontalPoster
import tv.trakt.trakt.core.summary.ui.header.PosterChipsGroup
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.extensions.isAtLeastLarge
import tv.trakt.trakt.ui.extensions.isAtLeastMedium
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
fun DetailsHeaderPosterHorizontal(
    imageUrl: String?,
    accentColor: Color?,
    loading: Boolean,
    creditsCount: Int?,
    playsCount: Int?,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val windowClass = currentWindowAdaptiveInfo().windowSizeClass

    Column(
        verticalArrangement = spacedBy(16.dp),
        modifier = modifier
            .ifOrElse(
                windowClass.isAtLeastMedium(),
                isTrue = when {
                    windowClass.isAtLeastLarge() -> Modifier.width(550.dp)
                    else -> Modifier.width(500.dp)
                },
                isFalse = Modifier,
            ),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_arrow),
                tint = TraktTheme.colors.textPrimary,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .onClick(onClick = onBackClick),
            )
            Icon(
                painter = painterResource(R.drawable.ic_share),
                tint = TraktTheme.colors.textPrimary,
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .onClick(onClick = onShareClick),
            )
        }

        Box {
            DetailsHorizontalPoster(
                imageUrl = imageUrl,
                color = accentColor,
            )

            this@Column.AnimatedVisibility(
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
                    personImdb = null,
                )
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
        DetailsHeaderPosterHorizontal(
            imageUrl = null,
            accentColor = null,
            loading = false,
            creditsCount = 3,
            playsCount = 0,
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
        DetailsHeaderPosterHorizontal(
            imageUrl = null,
            accentColor = null,
            loading = false,
            creditsCount = 3,
            playsCount = 0,
            onBackClick = {},
            onShareClick = {},
            modifier = Modifier
                .padding(bottom = 9.dp),
        )
    }
}

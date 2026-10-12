package tv.trakt.trakt.ui.components.yir

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.min
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.ui.theme.colors.Shade930
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.buttons.PrimaryButton
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.R as AppR

// The banner is always dark, matching the persona artwork, so colors are fixed instead of theme-aware.
private val BackgroundTop = Color(0xFF1C1730)
private val BackgroundBottom = Shade930
private val PosterShape = RoundedCornerShape(12.dp)

private const val POSTER_RATIO = 1200F / 1680F
private const val RIBBON_ROTATION = -6F

private data class Poster(
    @param:DrawableRes val drawable: Int,
    val rotation: Float,
)

private val TopRibbon = persistentListOf(
    Poster(AppR.drawable.yir_anime_voyager, rotation = -4F),
    Poster(AppR.drawable.yir_critic, rotation = 2F),
    Poster(AppR.drawable.yir_weekend_marathoner, rotation = -1F),
    Poster(AppR.drawable.yir_omnivore, rotation = 3F),
)

private val BottomRibbon = persistentListOf(
    Poster(AppR.drawable.yir_opening_act, rotation = 2F),
    Poster(AppR.drawable.yir_cinephile, rotation = -3F),
    Poster(AppR.drawable.yir_loyalist, rotation = 1F),
    Poster(AppR.drawable.yir_comfort_rewatcher, rotation = -2F),
)

@Composable
internal fun YearInReviewBanner(
    year: Int,
    modifier: Modifier = Modifier,
    onCheckClick: () -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BackgroundTop, BackgroundBottom)))
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_trakt_logo),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .padding(top = 24.dp)
                .height(32.dp),
        )

        PosterRibbons(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1F),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = spacedBy(40.dp),
            modifier = Modifier.padding(
                start = 24.dp,
                end = 24.dp,
                top = 16.dp,
                bottom = 32.dp,
            ),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.header_year_in_review_banner, year),
                    style = TraktTheme.typography.heading2.copy(
                        fontWeight = FontWeight.W700,
                        lineHeight = 1.1.em,
                        letterSpacing = (-0.02).em,
                    ),
                    color = TraktTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                )

                Text(
                    text = stringResource(R.string.text_year_in_review_banner_tagline),
                    style = TraktTheme.typography.paragraphSmall,
                    color = TraktTheme.colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }

            PrimaryButton(
                text = stringResource(R.string.button_text_check_now),
                onClick = onCheckClick,
                contentColor = Color.White,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PosterRibbons(modifier: Modifier = Modifier) {
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier.clipToBounds(),
    ) {
        val posterWidth = min(maxWidth * 0.32F, 180.dp)
        val posterSpacing = posterWidth * 0.08F

        Column(
            verticalArrangement = spacedBy(posterSpacing * 1.5F),
            modifier = Modifier.graphicsLayer {
                rotationZ = RIBBON_ROTATION
            },
        ) {
            PosterRibbon(
                posters = TopRibbon,
                posterWidth = posterWidth,
                spacing = posterSpacing,
                modifier = Modifier.offset(x = posterWidth * 0.1F),
            )
            PosterRibbon(
                posters = BottomRibbon,
                posterWidth = posterWidth,
                spacing = posterSpacing,
                modifier = Modifier.offset(x = -posterWidth * 0.25F),
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(posterWidth * 0.5F)
                .background(Brush.verticalGradient(listOf(Color.Transparent, BackgroundBottom))),
        )
    }
}

@Composable
private fun PosterRibbon(
    posters: ImmutableList<Poster>,
    posterWidth: Dp,
    spacing: Dp,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.wrapContentWidth(unbounded = true),
    ) {
        posters.forEach { poster ->
            Image(
                painter = painterResource(poster.drawable),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(posterWidth)
                    .aspectRatio(POSTER_RATIO)
                    .graphicsLayer { rotationZ = poster.rotation }
                    .shadow(8.dp, PosterShape)
                    .clip(PosterShape),
            )
        }
    }
}

@Preview(device = "id:pixel_5")
@Composable
private fun Preview() {
    TraktTheme {
        YearInReviewBanner(year = 2026)
    }
}

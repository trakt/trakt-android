package tv.trakt.trakt.ui.components.seasonal

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.ui.theme.colors.Green400
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.LocalSeasonalTheme
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.model.SeasonalTheme

private const val GLOW_ALPHA = 0.22F
private const val EVERGREEN_ALPHA = 0.14F
private const val NIGHT_ALPHA = 0.35F
private val NightColor = Color(0xFF132B5A)

@Composable
internal fun SeasonalDecor(modifier: Modifier = Modifier) {
    val season = LocalSeasonalTheme.current ?: return
    val glow = TraktTheme.colors.accentPalette.shade400.copy(alpha = GLOW_ALPHA)

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val brushes = when (season) {
                    SeasonalTheme.Halloween -> listOf(
                        Brush.radialGradient(
                            colors = listOf(glow, Color.Transparent),
                            center = Offset(size.width * 0.88F, -size.height * 0.04F),
                            radius = minOf(size.width * 0.36F, 480.dp.toPx()).coerceAtLeast(1F),
                        ),
                    )
                    SeasonalTheme.Christmas -> listOf(
                        Brush.radialGradient(
                            colors = listOf(glow, Color.Transparent),
                            center = Offset(size.width * 0.1F, -size.height * 0.08F),
                            radius = (size.width * 0.6F).coerceAtLeast(1F),
                        ),
                        Brush.radialGradient(
                            colors = listOf(Green400.copy(alpha = EVERGREEN_ALPHA), Color.Transparent),
                            center = Offset(size.width * 0.92F, -size.height * 0.07F),
                            radius = (size.width * 0.5F).coerceAtLeast(1F),
                        ),
                    )
                    SeasonalTheme.NewYear -> listOf(
                        Brush.radialGradient(
                            colors = listOf(glow, Color.Transparent),
                            center = Offset(size.width * 0.5F, -size.height * 0.2F),
                            radius = (size.width * 0.8F).coerceAtLeast(1F),
                        ),
                        Brush.verticalGradient(
                            0.7F to Color.Transparent,
                            1F to NightColor.copy(alpha = NIGHT_ALPHA),
                        ),
                    )
                }
                onDrawBehind {
                    brushes.forEach { drawRect(brush = it) }
                }
            },
    ) {
        val garland = when (season) {
            SeasonalTheme.Halloween -> null
            SeasonalTheme.Christmas -> R.drawable.img_christmas_garland to 24.dp
            SeasonalTheme.NewYear -> R.drawable.img_new_year_sparkles to 40.dp
        }

        if (garland != null) {
            Image(
                painter = painterResource(garland.first),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .fillMaxWidth()
                    .height(garland.second),
            )
        }
    }
}

@Composable
private fun SeasonalDecorPreview(season: SeasonalTheme) {
    TraktTheme(colors = season.darkColors) {
        CompositionLocalProvider(LocalSeasonalTheme provides season) {
            SeasonalDecor(
                modifier = Modifier.background(TraktTheme.colors.backgroundPrimary),
            )
        }
    }
}

@Preview(device = "id:pixel_5")
@Composable
private fun HalloweenPreview() {
    SeasonalDecorPreview(SeasonalTheme.Halloween)
}

@Preview(device = "id:pixel_5")
@Composable
private fun ChristmasPreview() {
    SeasonalDecorPreview(SeasonalTheme.Christmas)
}

@Preview(device = "id:pixel_5")
@Composable
private fun NewYearPreview() {
    SeasonalDecorPreview(SeasonalTheme.NewYear)
}

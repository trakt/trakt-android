package tv.trakt.trakt.ui.components.seasonal

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.ui.theme.colors.Shade10
import tv.trakt.trakt.common.ui.theme.colors.Shade900
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.LocalSeasonalTheme
import tv.trakt.trakt.ui.theme.TraktTheme
import tv.trakt.trakt.ui.theme.model.SeasonalTheme

private const val SANTA_HAT_TILT = 15F
private const val PARTY_HAT_TILT = 18F

private val MascotSize = 40.dp
private val FrostHeight = 4.dp
private val SnowPileHeight = 8.dp
private val SnowPileInset = 14.dp

/**
 * Wraps an action bar and decorates it for the active seasonal theme. Mascots overflow the
 * bar bounds, so the caller must leave room above it.
 */
@Composable
internal fun SeasonalActionBarDecor(
    shape: Shape,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val season = LocalSeasonalTheme.current

    Box(modifier = modifier) {
        if (season == SeasonalTheme.Halloween) {
            Image(
                painter = painterResource(R.drawable.img_ghost_mascot),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = -MascotSize * 0.4F, y = -MascotSize * 0.5F)
                    .size(MascotSize),
            )
        }

        content()

        when (season) {
            SeasonalTheme.Christmas -> ChristmasDecor(shape = shape)
            SeasonalTheme.NewYear -> PartyHat()
            SeasonalTheme.Halloween, null -> Unit
        }
    }
}

@Composable
private fun BoxScope.ChristmasDecor(shape: Shape) {
    val tilt = when (LocalLayoutDirection.current) {
        LayoutDirection.Ltr -> SANTA_HAT_TILT
        LayoutDirection.Rtl -> -SANTA_HAT_TILT
    }

    Box(
        modifier = Modifier
            .matchParentSize()
            .clip(shape)
            .drawBehind {
                drawRect(
                    color = Shade10,
                    size = Size(size.width, FrostHeight.toPx()),
                )
            },
    )

    Image(
        painter = painterResource(R.drawable.img_snow_pile),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .offset(y = -SnowPileHeight + 2.dp)
            .padding(horizontal = SnowPileInset)
            .fillMaxWidth(),
    )

    Image(
        painter = painterResource(R.drawable.img_santa_hat),
        contentDescription = null,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = MascotSize * 0.35F, y = -MascotSize * 0.55F)
            .size(width = MascotSize * 1.1F, height = MascotSize * 0.9F)
            .rotate(tilt),
    )
}

@Composable
private fun BoxScope.PartyHat() {
    val tilt = when (LocalLayoutDirection.current) {
        LayoutDirection.Ltr -> PARTY_HAT_TILT
        LayoutDirection.Rtl -> -PARTY_HAT_TILT
    }

    Image(
        painter = painterResource(R.drawable.img_party_hat),
        contentDescription = null,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = 7.dp, y = -34.dp)
            .size(width = 39.dp, height = 46.dp)
            .rotate(tilt),
    )
}

@Preview(widthDp = 400)
@Composable
private fun Preview() {
    TraktTheme {
        Column(
            modifier = Modifier
                .background(Shade900)
                .padding(horizontal = 32.dp, vertical = 16.dp),
        ) {
            SeasonalTheme.entries.forEach { season ->
                Spacer(modifier = Modifier.height(32.dp))
                CompositionLocalProvider(LocalSeasonalTheme provides season) {
                    SeasonalActionBarDecor(shape = RoundedCornerShape(100)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(TraktTheme.colors.dialogContainer, RoundedCornerShape(100)),
                        )
                    }
                }
            }
        }
    }
}

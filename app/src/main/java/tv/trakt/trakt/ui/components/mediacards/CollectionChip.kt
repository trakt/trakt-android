package tv.trakt.trakt.ui.components.mediacards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.trakt.trakt.common.helpers.extensions.ifOrElse
import tv.trakt.trakt.helpers.extensions.TraktThemeLightDark
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

// The chip is an overlay pinned to the bottom edge of a poster, so its metrics are
// tied to the artwork rather than to a spacing token.
private val ChipWidth = 24.dp
private val ChipHeight = 16.dp
private val ChipElevation = 1.5.dp
private val CountPadding = 5.dp
private val CountSpacing = 2.dp
private val CountFontSize = 9.sp

/**
 * The pill overlaying a poster with the user's relationship to a title: watched,
 * partially watched ([halved]) or in the watchlist.
 *
 * A [count] above one turns the pill into "icon + Nx", so a rewatched title reads as
 * rewatched straight from a list. At one or zero the pill keeps its fixed icon-only
 * size.
 */
@Composable
internal fun CollectionChip(
    iconRes: Int,
    iconSize: Dp,
    modifier: Modifier = Modifier,
    width: Dp = ChipWidth,
    height: Dp = ChipHeight,
    elevation: Dp = ChipElevation,
    halved: Boolean = false,
    count: Int = 0,
) {
    val shape = RoundedCornerShape(100)
    val countVisible = count > 1

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .ifOrElse(
                condition = countVisible,
                // The counter grows with the system font scale, so the pill can only
                // have a minimum height once it carries text.
                isTrue = Modifier.heightIn(min = height),
                isFalse = Modifier.size(width = width, height = height),
            )
            .shadow(elevation, shape)
            .clip(shape)
            .background(TraktTheme.colors.tagChipContainer),
    ) {
        if (halved) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f)
                    .align(Alignment.CenterEnd)
                    .background(TraktTheme.colors.tagChipContainerLight),
            )
        }

        Row(
            horizontalArrangement = spacedBy(CountSpacing),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .ifOrElse(
                    condition = countVisible,
                    isTrue = Modifier.padding(horizontal = CountPadding),
                ),
        ) {
            Icon(
                painter = painterResource(iconRes),
                tint = TraktTheme.colors.tagChipContent,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
            )

            if (countVisible) {
                Text(
                    text = "$count×",
                    color = TraktTheme.colors.tagChipContent,
                    style = TraktTheme.typography.meta.copy(fontSize = CountFontSize),
                )
            }
        }
    }
}

@Preview
@Composable
private fun CollectionChipPreview() {
    TraktThemeLightDark {
        Row(
            horizontalArrangement = spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            CollectionChip(
                iconRes = R.drawable.ic_check_double,
                iconSize = 11.5.dp,
            )
            CollectionChip(
                iconRes = R.drawable.ic_check_double,
                iconSize = 11.5.dp,
                halved = true,
            )
            CollectionChip(
                iconRes = R.drawable.ic_check_double,
                iconSize = 11.5.dp,
                count = 3,
            )
            CollectionChip(
                iconRes = R.drawable.ic_check_double,
                iconSize = 11.5.dp,
                count = 12,
            )
            CollectionChip(
                iconRes = R.drawable.ic_bookmark_on,
                iconSize = 11.5.dp,
            )
        }
    }
}

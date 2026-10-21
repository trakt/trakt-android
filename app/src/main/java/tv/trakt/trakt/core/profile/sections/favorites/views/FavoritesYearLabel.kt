package tv.trakt.trakt.core.profile.sections.favorites.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.core.favorites.FavoriteItem
import tv.trakt.trakt.ui.theme.TraktTheme
import java.time.ZoneId

private val YearLabelMinHeight = 20.dp

// Expects items sorted by listedAt, newest first, like web.
internal fun List<FavoriteItem>.toFavoritesYearHeaders(): ImmutableMap<String, Int> {
    val zone = ZoneId.systemDefault()
    return mapIndexedNotNull { index, item ->
        val year = item.listedAt.atZone(zone).year
        val previousYear = getOrNull(index - 1)?.listedAt?.atZone(zone)?.year
        if (year == previousYear) null else item.key to year
    }.toMap().toImmutableMap()
}

@Composable
internal fun FavoritesYearLabel(
    year: Int?,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .width(TraktTheme.size.verticalMediaCardSize)
            .heightIn(min = YearLabelMinHeight),
    ) {
        // An empty label still measures one text line, so every card in the row
        // reserves the same height at any font scale.
        if (year == null) {
            Text(
                text = "",
                style = TraktTheme.typography.meta,
                maxLines = 1,
            )
            return@Box
        }

        val lineColors = listOf(TraktTheme.colors.textSecondary, Color.Transparent)
        val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

        Row(
            horizontalArrangement = spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = year.toString(),
                color = TraktTheme.colors.textSecondary,
                style = TraktTheme.typography.meta,
                maxLines = 1,
            )

            Box(
                modifier = Modifier
                    .weight(1F)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            if (isRtl) lineColors.reversed() else lineColors,
                        ),
                    ),
            )
        }
    }
}

@Preview(
    showBackground = true,
    backgroundColor = 0xFF131517,
)
@Composable
private fun Preview() {
    TraktTheme {
        FavoritesYearLabel(year = 2026)
    }
}

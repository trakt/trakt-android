package tv.trakt.trakt.core.ratings.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.ui.theme.TraktTheme

internal val RatingSeparatorWidth = 1.dp
private val RatingSeparatorHeight = 16.dp

@Composable
internal fun RatingSeparator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = RatingSeparatorWidth, height = RatingSeparatorHeight)
            .background(TraktTheme.colors.separator),
    )
}

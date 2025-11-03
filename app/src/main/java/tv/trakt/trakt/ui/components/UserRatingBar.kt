package tv.trakt.trakt.ui.components

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun UserRatingBar(
    modifier: Modifier = Modifier,
    rating: Int? = null,
    size: Dp = 24.dp,
    onRatingClick: (Int) -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(8.dp, Alignment.CenterHorizontally),
        modifier = modifier,
    ) {
        repeat(5) { index ->
            Icon(
                painter = painterResource(
                    if (rating != null && index < rating) {
                        R.drawable.ic_star_trakt_on
                    } else {
                        R.drawable.ic_star_trakt_off
                    },
                ),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(size)
                    .onClick {
                        onRatingClick(index + 1)
                    },
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        UserRatingBar()
    }
}

@Preview
@Composable
private fun Preview2() {
    TraktTheme {
        UserRatingBar(
            rating = 3,
        )
    }
}

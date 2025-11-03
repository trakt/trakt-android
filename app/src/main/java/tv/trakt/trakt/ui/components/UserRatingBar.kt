package tv.trakt.trakt.ui.components

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.onClick
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun UserRatingBar(
    modifier: Modifier = Modifier,
    rating: UserRating? = null,
    size: Dp = 22.dp,
    onRatingClick: (Int) -> Unit = {},
) {
    val stars = remember(rating) {
        rating?.rating?.div(2f) ?: 0f
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(8.dp, Alignment.CenterHorizontally),
        modifier = modifier,
    ) {
        repeat(5) { index ->
            Icon(
                painter = painterResource(
                    when {
                        stars >= index + 1 -> R.drawable.ic_star_trakt_on
                        stars >= index + 0.5F -> R.drawable.ic_star_trakt_half
                        else -> R.drawable.ic_star_trakt_off
                    },
                ),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(size)
                    .onClick {
                        val scaledRating = UserRating.scaleTo10(index + 1)
                        if (rating?.rating != scaledRating) {
                            onRatingClick(scaledRating)
                        }
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
            rating = UserRating(
                mediaId = TraktId(1),
                mediaType = MediaType.MOVIE,
                rating = 7,
            ),
        )
    }
}

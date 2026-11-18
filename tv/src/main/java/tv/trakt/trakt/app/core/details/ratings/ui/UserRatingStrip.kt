package tv.trakt.trakt.app.core.details.ratings.ui

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import tv.trakt.trakt.app.core.details.ratings.UserRatingState
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.resources.R

@Composable
internal fun UserRatingStrip(
    state: UserRatingState,
    onRatingChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuVisible by remember { mutableStateOf(false) }
    val stars = (state.rating ?: 0) / 2f

    Box(modifier = modifier) {
        RatingSurface(
            loading = state.loading,
            onClick = { menuVisible = true },
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
        ) {
            Row(
                horizontalArrangement = spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
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
                        tint = TraktTheme.colors.textPrimary,
                        modifier = Modifier.size(RatingIconSize),
                    )
                }
            }
        }

        RatingMenu(
            expanded = menuVisible,
            rating = state.rating,
            onDismissRequest = { menuVisible = false },
            onSelect = onRatingChange,
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        UserRatingStrip(
            state = UserRatingState(rating = 7),
            onRatingChange = {},
        )
    }
}

@Preview
@Composable
private fun PreviewEmpty() {
    TraktTheme {
        UserRatingStrip(
            state = UserRatingState(),
            onRatingChange = {},
        )
    }
}

@Preview
@Composable
private fun PreviewLoading() {
    TraktTheme {
        UserRatingStrip(
            state = UserRatingState(rating = 4, loading = true),
            onRatingChange = {},
        )
    }
}

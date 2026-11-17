package tv.trakt.trakt.app.core.details.ratings.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import tv.trakt.trakt.app.core.details.ratings.UserRatingState
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.ui.composables.FilmProgressIndicator
import tv.trakt.trakt.resources.R

private val StripShape = RoundedCornerShape(12.dp)
private val StarSize = 24.dp

@Composable
internal fun UserRatingStrip(
    state: UserRatingState,
    onRatingChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuVisible by remember { mutableStateOf(false) }
    val stars = (state.rating ?: 0) / 2f

    Box(modifier = modifier) {
        Surface(
            onClick = {
                if (!state.loading) menuVisible = true
            },
            shape = ClickableSurfaceDefaults.shape(shape = StripShape),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                pressedContainerColor = Color.Transparent,
                contentColor = TraktTheme.colors.textPrimary,
                focusedContentColor = TraktTheme.colors.textPrimary,
                pressedContentColor = TraktTheme.colors.textPrimary,
            ),
            border = ClickableSurfaceDefaults.border(
                focusedBorder = Border(
                    border = BorderStroke(width = 2.75.dp, color = TraktTheme.colors.primaryButtonContent),
                    shape = StripShape,
                ),
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1F),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Row(
                    horizontalArrangement = spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .alpha(if (state.loading) 0F else 1F),
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
                            modifier = Modifier.size(StarSize),
                        )
                    }
                }

                if (state.loading) {
                    FilmProgressIndicator(
                        size = 18.dp,
                        color = TraktTheme.colors.textSecondary,
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

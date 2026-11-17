package tv.trakt.trakt.app.core.details.ratings.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.ui.theme.colors.Red500
import tv.trakt.trakt.resources.R

@Composable
internal fun FavoriteButton(
    favorite: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RatingSurface(
        loading = loading,
        onClick = onClick,
        contentPadding = PaddingValues(7.dp),
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(if (favorite) R.drawable.ic_heart_on else R.drawable.ic_heart_off),
            contentDescription = null,
            tint = if (favorite) Red500 else TraktTheme.colors.textPrimary,
            modifier = Modifier.size(RatingIconSize),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    TraktTheme {
        FavoriteButton(favorite = false, loading = false, onClick = {})
    }
}

@Preview
@Composable
private fun PreviewFavorite() {
    TraktTheme {
        FavoriteButton(favorite = true, loading = false, onClick = {})
    }
}

@Preview
@Composable
private fun PreviewLoading() {
    TraktTheme {
        FavoriteButton(favorite = true, loading = true, onClick = {})
    }
}

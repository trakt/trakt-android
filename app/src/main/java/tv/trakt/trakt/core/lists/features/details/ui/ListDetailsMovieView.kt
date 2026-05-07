package tv.trakt.trakt.core.lists.features.details.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.movies.ui.MovieMetaFooter
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard

@Composable
internal fun ListDetailsMovieView(
    item: CustomListItem.MovieItem,
    modifier: Modifier = Modifier,
    showIcon: Boolean = false,
    shadow: Boolean = false,
    enabled: Boolean = true,
    watched: Boolean = false,
    watchlist: Boolean = false,
    onClick: (TraktId) -> Unit = { },
    onLongClick: () -> Unit,
) {
    val genresText = item.movie.genres.take(2)
        .map { stringResource(it.displayStringRes) }
        .joinToString(", ")

    PanelMediaCard(
        modifier = modifier,
        title = item.movie.title,
        titleOriginal = item.movie.titleOriginal,
        subtitle = genresText,
        shadow = if (shadow) 4.dp else 0.dp,
        enabled = enabled,
        watched = watched,
        watchlist = watchlist,
        contentImageUrl = item.movie.images?.getPosterUrl(),
        containerImageUrl = item.images?.getFanartUrl(Images.Size.THUMB),
        onClick = { onClick(item.movie.ids.trakt) },
        onLongClick = onLongClick,
        footerContent = {
            MovieMetaFooter(
                movie = item.movie,
                mediaIcon = showIcon,
                loading = item.loading,
            )
        },
    )
}

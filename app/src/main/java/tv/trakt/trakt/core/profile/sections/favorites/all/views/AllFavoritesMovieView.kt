package tv.trakt.trakt.core.profile.sections.favorites.all.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import tv.trakt.trakt.common.core.favorites.FavoriteItem
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.sorting.SortType.Runtime
import tv.trakt.trakt.common.model.sorting.SortType.UserRating
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.movies.ui.MovieMetaFooter
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard

@Composable
internal fun AllFavoritesMovieView(
    item: FavoriteItem.MovieItem,
    sorting: Sorting,
    modifier: Modifier = Modifier,
    watched: Boolean = false,
    watchlist: Boolean = false,
    plays: Int = 0,
    mediaIcon: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit = {},
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
        watched = watched,
        watchlist = watchlist,
        plays = plays,
        enabled = !loading,
        contentImageUrl = item.movie.images?.getPosterUrl(),
        containerImageUrl = item.images?.getFanartUrl(Images.Size.THUMB),
        onClick = onClick,
        onLongClick = onLongClick,
        onImageClick = onClick,
        footerContent = {
            MovieMetaFooter(
                movie = item.movie,
                mediaIcon = mediaIcon,
                rating = sorting.type != UserRating && sorting.type != Runtime,
                userRating = when {
                    sorting.type == UserRating -> item.userRating
                    else -> null
                },
                duration = when {
                    sorting.type == Runtime -> item.movie.runtime
                    else -> null
                },
            )
        },
    )
}

package tv.trakt.trakt.core.lists.sections.watchlist.features.all.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import tv.trakt.trakt.common.core.lists.model.WatchlistItem
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.sorting.SortType.Runtime
import tv.trakt.trakt.common.model.sorting.SortType.UserRating
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.movies.ui.MovieMetaFooter
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard

@Composable
internal fun AllWatchlistMovieView(
    item: WatchlistItem.MovieItem,
    sorting: Sorting,
    modifier: Modifier = Modifier,
    showCheck: Boolean = false,
    watched: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    onCheckClick: () -> Unit,
    onCheckLongClick: () -> Unit,
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
        enabled = enabled,
        contentImageUrl = item.movie.images?.getPosterUrl(),
        containerImageUrl = item.images?.getFanartUrl(Images.Size.THUMB),
        onClick = onClick,
        onLongClick = onLongClick,
        onImageClick = onClick,
        footerContent = {
            MovieMetaFooter(
                movie = item.movie,
                mediaIcon = true,
                rating = enabled && sorting.type != UserRating && sorting.type != Runtime,
                userRating = when {
                    sorting.type == UserRating && enabled -> item.userRating
                    else -> null
                },
                duration = when {
                    sorting.type == Runtime && enabled -> item.movie.runtime
                    else -> null
                },
                loading = item.loading,
                check = showCheck,
                onCheckClick = onCheckClick,
                onCheckLongClick = onCheckLongClick,
            )
        },
    )
}

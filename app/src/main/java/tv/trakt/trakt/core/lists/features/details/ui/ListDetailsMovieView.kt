package tv.trakt.trakt.core.lists.features.details.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.model.sorting.SortType.Runtime
import tv.trakt.trakt.common.model.sorting.SortType.UserRating
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.movies.ui.MovieMetaFooter
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard

@Composable
internal fun ListDetailsMovieView(
    movie: Movie,
    movieUserRating: UserRating?,
    sorting: Sorting,
    modifier: Modifier = Modifier,
    showIcon: Boolean = false,
    shadow: Boolean = false,
    enabled: Boolean = true,
    watched: Boolean = false,
    watchlist: Boolean = false,
    onClick: (TraktId) -> Unit = { },
    onLongClick: () -> Unit,
) {
    val genresText = movie.genres.take(2)
        .map { stringResource(it.displayStringRes) }
        .joinToString(", ")

    PanelMediaCard(
        modifier = modifier,
        title = movie.title,
        titleOriginal = movie.titleOriginal,
        subtitle = genresText,
        shadow = if (shadow) 4.dp else 0.dp,
        enabled = enabled,
        watched = watched,
        watchlist = watchlist,
        contentImageUrl = movie.images?.getPosterUrl(),
        containerImageUrl = movie.images?.getFanartUrl(Images.Size.THUMB),
        onClick = { onClick(movie.ids.trakt) },
        onImageClick = { onClick(movie.ids.trakt) },
        onLongClick = onLongClick,
        footerContent = {
            MovieMetaFooter(
                movie = movie,
                mediaIcon = showIcon,
                rating = enabled && sorting.type != UserRating && sorting.type != Runtime,
                userRating = when {
                    sorting.type == UserRating && enabled -> movieUserRating
                    else -> null
                },
                duration = when {
                    sorting.type == Runtime && enabled -> movie.runtime
                    else -> null
                },
            )
        },
    )
}

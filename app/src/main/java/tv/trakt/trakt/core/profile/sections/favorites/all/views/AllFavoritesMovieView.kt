package tv.trakt.trakt.core.profile.sections.favorites.all.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.core.movies.ui.MovieMetaFooter
import tv.trakt.trakt.core.profile.model.FavoriteItem
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard

@Composable
internal fun AllFavoritesMovieView(
    item: FavoriteItem.MovieItem,
    modifier: Modifier = Modifier,
    mediaIcon: Boolean = true,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit,
) {
    val genresText = remember(item.movie.genres) {
        item.movie.genres.take(2).joinToString(", ") { genre ->
            genre.replaceFirstChar {
                it.uppercaseChar()
            }
        }
    }

    PanelMediaCard(
        modifier = modifier,
        title = item.movie.title,
        titleOriginal = item.movie.titleOriginal,
        subtitle = genresText,
        contentImageUrl = item.movie.images?.getPosterUrl(),
        containerImageUrl = item.images?.getFanartUrl(Images.Size.THUMB),
        onClick = onClick,
        onLongClick = onLongClick,
        footerContent = {
            MovieMetaFooter(
                movie = item.movie,
                mediaIcon = mediaIcon,
            )
        },
    )
}

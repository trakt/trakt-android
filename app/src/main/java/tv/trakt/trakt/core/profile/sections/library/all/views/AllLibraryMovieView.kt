package tv.trakt.trakt.core.profile.sections.library.all.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.core.library.model.LibraryItem
import tv.trakt.trakt.core.movies.ui.MovieMetaFooter
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard

@Composable
internal fun AllLibraryMovieView(
    item: LibraryItem.MovieItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
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
        containerImageUrl = item.movie.images?.getFanartUrl(Images.Size.THUMB),
        more = false,
        onClick = onClick,
        footerContent = {
            MovieMetaFooter(
                movie = item.movie,
                mediaIcon = true,
            )
        },
    )
}

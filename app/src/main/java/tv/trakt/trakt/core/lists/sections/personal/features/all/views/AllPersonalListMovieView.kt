package tv.trakt.trakt.core.lists.sections.personal.features.all.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import tv.trakt.trakt.common.model.Images.Size
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.PersonalListItem
import tv.trakt.trakt.core.movies.ui.MovieMetaFooter
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard

@Composable
internal fun AllPersonalListMovieView(
    item: PersonalListItem.MovieItem,
    modifier: Modifier = Modifier,
    showIcon: Boolean = false,
    watched: Boolean = false,
    watchlist: Boolean = false,
    onClick: (TraktId) -> Unit,
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
        containerImageUrl = item.images?.getFanartUrl(Size.THUMB),
        watched = watched,
        watchlist = watchlist,
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

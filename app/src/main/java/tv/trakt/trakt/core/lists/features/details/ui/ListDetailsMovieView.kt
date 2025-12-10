package tv.trakt.trakt.core.lists.features.details.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.PersonalListItem
import tv.trakt.trakt.core.movies.ui.MovieMetaFooter
import tv.trakt.trakt.ui.components.mediacards.PanelMediaCard

@Composable
internal fun ListDetailsMovieView(
    item: PersonalListItem.MovieItem,
    modifier: Modifier = Modifier,
    showIcon: Boolean = false,
    shadow: Boolean = false,
    enabled: Boolean = true,
    onClick: (TraktId) -> Unit = { },
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
        shadow = if (shadow) 4.dp else 0.dp,
        enabled = enabled,
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

package tv.trakt.trakt.core.search.views.items

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.helpers.extensions.rememberDurationFormat
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.core.search.model.SearchFilter
import tv.trakt.trakt.core.search.model.SearchItem
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.mediacards.VerticalMediaCard
import tv.trakt.trakt.ui.theme.TraktTheme

@Composable
internal fun SearchMovieGridItem(
    item: SearchItem.Movie,
    filter: SearchFilter,
    watched: Boolean,
    watching: Boolean,
    watchlist: Boolean,
    plays: Int,
    onMovieClick: (Movie) -> Unit,
    onMovieLongClick: (Movie) -> Unit = {},
    modifier: Modifier,
) {
    var currentFilter by remember { mutableStateOf(filter) }

    LaunchedEffect(filter) {
        if (filter != SearchFilter.PEOPLE) {
            currentFilter = filter
        }
    }

    VerticalMediaCard(
        title = item.movie.title,
        imageUrl = item.movie.images?.getPosterUrl(),
        watched = watched,
        watching = watching,
        watchlist = watchlist,
        plays = plays,
        chipSpacing = 10.dp,
        chipContent = { modifier ->
            Row(
                verticalAlignment = CenterVertically,
                horizontalArrangement = spacedBy(4.dp),
                modifier = modifier,
            ) {
                if (currentFilter == SearchFilter.MEDIA) {
                    Icon(
                        painter = painterResource(R.drawable.ic_movies_off),
                        contentDescription = null,
                        tint = TraktTheme.colors.chipContent,
                        modifier = Modifier
                            .size(13.dp)
                            .graphicsLayer {
                                translationY = -(0.25).dp.toPx()
                            },
                    )
                }

                val runtime = rememberDurationFormat(item.movie.runtime?.inWholeMinutes)
                Text(
                    text = remember {
                        val year = item.movie.year?.toString() ?: "TBA"
                        if (runtime != "N/A") {
                            "$year • $runtime"
                        } else {
                            year
                        }
                    },
                    style = TraktTheme.typography.cardTitle,
                    color = TraktTheme.colors.textPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        onClick = { onMovieClick(item.movie) },
        onLongClick = { onMovieLongClick(item.movie) },
        modifier = modifier,
    )
}

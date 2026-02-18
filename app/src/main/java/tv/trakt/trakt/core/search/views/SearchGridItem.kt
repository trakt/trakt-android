package tv.trakt.trakt.core.search.views

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.search.model.SearchFilter
import tv.trakt.trakt.core.search.model.SearchItem
import tv.trakt.trakt.core.search.views.items.SearchListGridItem
import tv.trakt.trakt.core.search.views.items.SearchMovieGridItem
import tv.trakt.trakt.core.search.views.items.SearchPersonGridItem
import tv.trakt.trakt.core.search.views.items.SearchShowGridItem

@Composable
internal fun SearchGridItem(
    item: SearchItem,
    filter: SearchFilter,
    modifier: Modifier = Modifier,
    watched: Boolean = false,
    watchlist: Boolean = false,
    onShowClick: (Show) -> Unit = {},
    onMovieClick: (Movie) -> Unit = {},
    onShowLongClick: (Show) -> Unit = {},
    onMovieLongClick: (Movie) -> Unit = {},
    onPersonClick: (Person) -> Unit = {},
    onListClick: (CustomList) -> Unit = {},
) {
    when (item) {
        is SearchItem.Show -> SearchShowGridItem(
            item = item,
            filter = filter,
            watched = watched,
            watchlist = watchlist,
            onShowClick = onShowClick,
            onShowLongClick = onShowLongClick,
            modifier = modifier,
        )

        is SearchItem.Movie -> SearchMovieGridItem(
            item = item,
            filter = filter,
            watched = watched,
            watchlist = watchlist,
            onMovieClick = onMovieClick,
            onMovieLongClick = onMovieLongClick,
            modifier = modifier,
        )

        is SearchItem.Person -> SearchPersonGridItem(
            item = item,
            onPersonClick = onPersonClick,
            modifier = modifier,
        )

        is SearchItem.List -> SearchListGridItem(
            item = item,
            onListClick = onListClick,
            modifier = modifier,
        )
    }
}

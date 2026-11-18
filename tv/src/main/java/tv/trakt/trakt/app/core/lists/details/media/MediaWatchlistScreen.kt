package tv.trakt.trakt.app.core.lists.details.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.app.core.details.lists.ListItemCount
import tv.trakt.trakt.app.core.lists.details.personal.PersonalListContent
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.resources.R

@Composable
internal fun MediaWatchlistScreen(
    viewModel: MediaWatchlistViewModel,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    PersonalListContent(
        state = state,
        listName = stringResource(R.string.list_title_watchlist),
        listSubtitle = {
            state.collection.watchlistCount(MediaMode.Media)
                .takeIf { it > 0 }
                ?.let { ListItemCount(count = it) }
        },
        onShowClick = onNavigateToShow,
        onMovieClick = onNavigateToMovie,
        onEpisodeClick = { _, _ -> },
        onLoadNextPage = viewModel::loadNextDataPage,
        onSortingChange = viewModel::setSorting,
        showWatchlist = false,
    )
}

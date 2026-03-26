package tv.trakt.trakt.core.lists.sections.collaborations

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.sections.liked.ListsLikedState
import tv.trakt.trakt.core.lists.sections.liked.ListsPersonalContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ListsCollaborationsView(
    viewModel: ListsCollaborationsViewModel,
    headerPadding: PaddingValues,
    contentPadding: PaddingValues,
    onAllClick: (CustomList) -> Unit,
    onMovieClick: (TraktId) -> Unit,
    onShowClick: (TraktId) -> Unit,
    onEpisodeClick: (showId: TraktId, episode: Episode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.navigateMovie, state.navigateShow, state.navigateEpisode) {
        state.navigateShow?.let {
            onShowClick(it)
            viewModel.clearNavigation()
        }
        state.navigateMovie?.let {
            onMovieClick(it)
            viewModel.clearNavigation()
        }
        state.navigateEpisode?.let { (showId, episode) ->
            onEpisodeClick(showId, episode)
            viewModel.clearNavigation()
        }
    }

    ListsPersonalContent(
        state = state.asLikedState(),
        list = state.list,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        onCollapse = viewModel::setCollapsed,
        onMovieClick = {
            if (!state.loading.isLoading) {
                viewModel.navigateToMovie(it)
            }
        },
        onShowClick = {
            if (!state.loading.isLoading) {
                viewModel.navigateToShow(it)
            }
        },
        onEpisodeClick = { show, episode ->
            if (!state.loading.isLoading) {
                viewModel.navigateToEpisode(show, episode)
            }
        },
        onAllClick = {
            state.list?.let {
                if (!state.items.isNullOrEmpty()) {
                    onAllClick(it)
                }
            }
        },
    )
}

private fun ListsCollaborationsState.asLikedState() =
    ListsLikedState(
        user = user,
        list = list,
        items = items,
        filter = filter,
        collection = collection,
        navigateShow = navigateShow,
        navigateMovie = navigateMovie,
        navigateEpisode = navigateEpisode,
        loading = loading,
        collapsed = collapsed,
        error = error,
    )

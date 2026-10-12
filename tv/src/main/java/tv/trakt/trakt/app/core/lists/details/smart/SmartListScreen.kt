package tv.trakt.trakt.app.core.lists.details.smart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.Text
import tv.trakt.trakt.app.core.lists.details.personal.PersonalListContent
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.TraktId

@Composable
internal fun SmartListScreen(
    viewModel: SmartListViewModel,
    onNavigateToShow: (TraktId) -> Unit,
    onNavigateToMovie: (TraktId) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val list = viewModel.list

    PersonalListContent(
        state = state,
        listName = list.name,
        listSubtitle = {
            Text(
                text = list.rememberDescription(),
                style = TraktTheme.typography.paragraphSmall,
                color = TraktTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        onShowClick = onNavigateToShow,
        onMovieClick = onNavigateToMovie,
        onLoadNextPage = viewModel::loadNextDataPage,
    )
}

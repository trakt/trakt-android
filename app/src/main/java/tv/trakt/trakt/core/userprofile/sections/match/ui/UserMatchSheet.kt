@file:OptIn(ExperimentalMaterial3Api::class)

package tv.trakt.trakt.core.userprofile.sections.match.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue.Expanded
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.userprofile.sections.match.UserMatchState
import tv.trakt.trakt.ui.components.TraktBottomSheet

private const val SHEET_MAX_HEIGHT_FRACTION = 0.75F

@Composable
internal fun UserMatchSheet(
    state: SheetState = rememberBottomSheetState(
        initialValue = Hidden,
        enabledValues = setOf(Hidden, Expanded),
    ),
    matchState: UserMatchState,
    user: User,
    visible: Boolean,
    onMovieClick: (Movie) -> Unit,
    onShowClick: (Show) -> Unit,
    onDismiss: () -> Unit,
) {
    val maxHeight = LocalWindowInfo.current.containerDpSize.height * SHEET_MAX_HEIGHT_FRACTION
    val match = matchState.match
    if (visible && match != null) {
        TraktBottomSheet(
            sheetState = state,
            contentWindowInsets = { WindowInsets() },
            onDismiss = onDismiss,
        ) {
            UserMatchView(
                match = match,
                viewer = matchState.viewer,
                user = user,
                sharedMovies = matchState.sharedMovies,
                sharedShows = matchState.sharedShows,
                onMovieClick = onMovieClick,
                onShowClick = onShowClick,
                modifier = Modifier
                    .heightIn(max = maxHeight)
                    .padding(horizontal = 24.dp),
            )
        }
    }
}

package tv.trakt.trakt.app.core.home.sections.movies.availablenow.viewall

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.home.sections.movies.availablenow.model.WatchlistMovie

@Immutable
internal data class AvailableNowViewAllState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val items: ImmutableList<WatchlistMovie>? = null,
    val error: Exception? = null,
)

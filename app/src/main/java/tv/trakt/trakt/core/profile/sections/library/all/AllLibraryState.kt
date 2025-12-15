package tv.trakt.trakt.core.profile.sections.library.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.library.model.LibraryFilter
import tv.trakt.trakt.core.library.model.LibraryItem

@Immutable
internal data class AllLibraryState(
    val filter: LibraryFilter? = null,
    val items: ImmutableList<LibraryItem>? = null,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)

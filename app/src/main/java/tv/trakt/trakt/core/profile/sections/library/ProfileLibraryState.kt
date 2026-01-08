package tv.trakt.trakt.core.profile.sections.library

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.library.model.LibraryFilter
import tv.trakt.trakt.core.library.model.LibraryItem

@Immutable
internal data class ProfileLibraryState(
    val user: User? = null,
    val items: ImmutableList<LibraryItem>? = null,
    val filter: LibraryFilter? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val collapsed: Boolean? = null,
    val error: Exception? = null,
)

package tv.trakt.trakt.app.core.profile.sections.library

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.profile.sections.library.model.LibraryItem

@Immutable
internal data class ProfileLibraryState(
    val items: ImmutableList<LibraryItem>? = null,
    val isLoading: Boolean = true,
    val error: Exception? = null,
)

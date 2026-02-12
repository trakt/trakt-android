package tv.trakt.trakt.app.core.profile.sections.library.viewall

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.profile.sections.library.model.LibraryItem

@Immutable
internal data class ProfileLibraryViewAllState(
    val isLoading: Boolean = false,
    val isLoadingPage: Boolean = false,
    val items: ImmutableList<LibraryItem>? = null,
    val error: Exception? = null,
)

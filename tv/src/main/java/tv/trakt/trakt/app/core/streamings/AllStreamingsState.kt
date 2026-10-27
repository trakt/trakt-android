package tv.trakt.trakt.app.core.streamings

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.core.streamings.model.AllStreamingsSection
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.streamings.YouTubeSpecial

@Immutable
internal data class AllStreamingsState(
    val sections: ImmutableList<AllStreamingsSection> = EmptyImmutableList,
    val youTubeSpecial: YouTubeSpecial? = null,
    val loading: Boolean = true,
    val error: Exception? = null,
)

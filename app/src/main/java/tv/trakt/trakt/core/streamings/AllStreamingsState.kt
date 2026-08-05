package tv.trakt.trakt.core.streamings

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.core.streamings.model.AllStreamingsSection
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList

@Immutable
internal data class AllStreamingsState(
    val sections: ImmutableList<AllStreamingsSection> = EmptyImmutableList,
    val media: Media = Media(),
    val loading: LoadingState = Idle,
    val error: Exception? = null,
) {
    @Immutable
    internal data class Media(
        val title: String? = null,
        val background: String? = null,
    )
}

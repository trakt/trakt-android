package tv.trakt.trakt.core.home.sections.upnext

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.core.home.sections.upnext.model.UpNextItem

@Immutable
internal data class HomeUpNextState(
    val items: ItemsState = ItemsState(),
    val loading: LoadingState = LoadingState.Idle,
    val collapsed: Boolean? = null,
    val info: StringResource? = null,
    val error: Exception? = null,
) {
    @Immutable
    data class ItemsState(
        val items: ImmutableList<UpNextItem>? = null,
        val resetScroll: Boolean = true,
    )
}

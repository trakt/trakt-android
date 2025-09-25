package tv.trakt.trakt.core.home.sections.upnext.features.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.core.home.sections.upnext.model.ProgressShow

@Immutable
internal data class AllHomeUpNextState(
    val items: ImmutableList<ProgressShow>? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val loadingMore: LoadingState = LoadingState.IDLE,
    val backgroundUrl: String? = null,
    val info: StringResource? = null,
    val error: Exception? = null,
)

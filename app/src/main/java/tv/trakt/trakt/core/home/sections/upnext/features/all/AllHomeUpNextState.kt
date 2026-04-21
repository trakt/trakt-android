package tv.trakt.trakt.core.home.sections.upnext.features.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.home.sections.upnext.model.UpNextItem
import tv.trakt.trakt.core.main.model.MediaMode

@Immutable
internal data class AllHomeUpNextState(
    val items: ImmutableList<UpNextItem>? = null,
    val loading: LoadingState = LoadingState.Idle,
    val loadingMore: LoadingState = LoadingState.Idle,
    val filter: MediaMode? = null,
    val user: User? = null,
    val info: StringResource? = null,
    val error: Exception? = null,
)

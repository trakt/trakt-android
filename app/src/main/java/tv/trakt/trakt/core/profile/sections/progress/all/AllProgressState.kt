package tv.trakt.trakt.core.profile.sections.progress.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.profile.sections.progress.model.ProfileProgressItem
import tv.trakt.trakt.core.profile.sections.progress.model.ProgressFilter

@Immutable
internal data class AllProgressState(
    val filter: ProgressFilter? = null,
    val items: ImmutableList<ProfileProgressItem>? = null,
    val navigateShow: TraktId? = null,
    val user: User? = null,
    val loading: LoadingState = LoadingState.Idle,
    val loadingMore: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
)

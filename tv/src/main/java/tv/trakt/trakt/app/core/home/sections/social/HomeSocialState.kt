package tv.trakt.trakt.app.core.home.sections.social

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.activity.HomeActivityItem

@Immutable
internal data class HomeSocialState(
    val items: ImmutableList<HomeActivityItem>? = null,
    val isLoading: Boolean = true,
    val error: Exception? = null,
)

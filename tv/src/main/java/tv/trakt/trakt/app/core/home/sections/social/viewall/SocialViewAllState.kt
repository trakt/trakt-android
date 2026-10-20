package tv.trakt.trakt.app.core.home.sections.social.viewall

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.model.activity.HomeActivityItem

@Immutable
internal data class SocialViewAllState(
    val isLoading: Boolean = false,
    val items: ImmutableList<HomeActivityItem>? = null,
    val error: Exception? = null,
)

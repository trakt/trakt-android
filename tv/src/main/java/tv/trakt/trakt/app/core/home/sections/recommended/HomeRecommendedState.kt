package tv.trakt.trakt.app.core.home.sections.recommended

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.app.core.home.sections.recommended.model.RecommendedItem
import tv.trakt.trakt.common.core.user.UserCollectionState

@Immutable
internal data class HomeRecommendedState(
    val items: ImmutableList<RecommendedItem>? = null,
    val isLoading: Boolean = true,
    val collection: UserCollectionState = UserCollectionState.Default,
    val error: Exception? = null,
)

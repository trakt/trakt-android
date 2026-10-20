package tv.trakt.trakt.app.core.home.sections.recommended

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.model.recommended.RecommendedItem

@Immutable
internal data class HomeRecommendedState(
    val items: ImmutableList<RecommendedItem>? = null,
    val isLoading: Boolean = true,
    val collection: UserCollectionState = UserCollectionState.Default,
    val error: Exception? = null,
)

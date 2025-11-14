package tv.trakt.trakt.core.lists.features.details

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.PersonalListItem

@Immutable
internal data class ListDetailsState(
    val list: ListDetailsInfo? = null,
    val items: ImmutableList<PersonalListItem>? = null,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
) {
    data class ListDetailsInfo(
        val mediaId: TraktId,
        val name: String,
        val description: String?,
    )
}

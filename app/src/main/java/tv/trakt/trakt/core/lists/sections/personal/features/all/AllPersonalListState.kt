package tv.trakt.trakt.core.lists.sections.personal.features.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.model.PersonalListItem

@Immutable
internal data class AllPersonalListState(
    val backgroundUrl: String? = null,
    val list: CustomList? = null,
    val items: ImmutableList<PersonalListItem>? = null,
    val navigateMovie: TraktId? = null,
    val loading: LoadingState = LoadingState.IDLE,
    val error: Exception? = null,
)

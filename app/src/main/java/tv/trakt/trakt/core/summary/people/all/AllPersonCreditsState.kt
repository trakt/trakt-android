package tv.trakt.trakt.core.summary.people.all

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.summary.people.model.PersonCreditItem
import tv.trakt.trakt.core.summary.people.model.PersonCreditsSection

@Immutable
internal data class AllPersonCreditsState(
    val section: PersonCreditsSection = PersonCreditsSection.Movies,
    val mode: MediaMode = MediaMode.Media,
    val person: Person? = null,
    val positions: ImmutableList<String> = persistentListOf(),
    val selectedPosition: String? = null,
    val items: ImmutableList<PersonCreditItem> = persistentListOf(),
    val collection: UserCollectionState = UserCollectionState.Default,
    val loading: LoadingState = LoadingState.Idle,
    val navigateShow: TraktId? = null,
    val navigateMovie: TraktId? = null,
    val error: Exception? = null,
)

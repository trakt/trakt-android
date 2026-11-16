package tv.trakt.trakt.core.lists.features.details.info

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.Person

@Immutable
internal data class ListInfoState(
    val people: ImmutableList<Person>? = null,
    val loading: LoadingState = LoadingState.Idle,
)

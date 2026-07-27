package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList

@Immutable
data class CrewPerson(
    val person: Person,
    val jobs: ImmutableList<String> = EmptyImmutableList,
    val episodesCount: Int = 0,
)

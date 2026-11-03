package tv.trakt.trakt.core.home.sections.recommended.whythis

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.model.recommended.RecommendedPerson
import tv.trakt.trakt.common.model.recommended.RecommendedSource

@Immutable
internal data class WhyThisState(
    val sources: ImmutableList<RecommendedSource> = persistentListOf(),
    val people: ImmutableList<RecommendedPerson> = persistentListOf(),
    val loading: LoadingState = LoadingState.Idle,
)

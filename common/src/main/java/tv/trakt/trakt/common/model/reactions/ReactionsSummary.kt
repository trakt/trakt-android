package tv.trakt.trakt.common.model.reactions

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap

@Immutable
data class ReactionsSummary(
    val reactionsCount: Int,
    val distribution: ImmutableMap<Reaction, Int>,
)

package tv.trakt.trakt.core.mixtape.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.TraktId

@Immutable
internal sealed interface MixtapeTarget {
    data object Watchlist : MixtapeTarget

    @Immutable
    data class PersonalList(
        val id: TraktId,
        val ownerId: TraktId,
        val name: String,
    ) : MixtapeTarget
}

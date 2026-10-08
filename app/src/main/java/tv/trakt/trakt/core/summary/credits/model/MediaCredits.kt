package tv.trakt.trakt.core.summary.credits.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.CastPerson
import tv.trakt.trakt.common.model.CrewPerson

/**
 * Supporting cast is only populated when there is also a main cast to split from.
 * Otherwise every cast member lives in [main].
 */
@Immutable
internal data class MediaCredits(
    val main: ImmutableList<CastPerson> = EmptyImmutableList,
    val supporting: ImmutableList<CastPerson> = EmptyImmutableList,
    val crew: ImmutableList<CrewPerson> = EmptyImmutableList,
) {
    val modes: ImmutableList<CreditsMode>
        get() = CreditsMode.entries
            .filter { mode ->
                when (mode) {
                    CreditsMode.Main -> main.isNotEmpty()
                    CreditsMode.Supporting -> supporting.isNotEmpty()
                    CreditsMode.Crew -> crew.isNotEmpty()
                }
            }
            .toImmutableList()

    val isSplit: Boolean
        get() = supporting.isNotEmpty()
}

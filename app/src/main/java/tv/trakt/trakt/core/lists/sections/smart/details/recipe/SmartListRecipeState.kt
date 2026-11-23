package tv.trakt.trakt.core.lists.sections.smart.details.recipe

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.model.lists.AwardCeremony

@Immutable
internal data class SmartListRecipeState(
    val awardCeremonies: ImmutableList<AwardCeremony> = EmptyImmutableList,
)

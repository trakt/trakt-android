package tv.trakt.trakt.core.home.sections.nudge

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.core.home.sections.nudge.model.VipNudge

@Immutable
internal data class HomeNudgeState(
    val nudge: VipNudge? = null,
)

package tv.trakt.trakt.core.lists.features.convert.nudge

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.lists.SmartListDraftsSummary

@Immutable
internal data class SmartListNudgeState(
    val summary: SmartListDraftsSummary? = null,
)

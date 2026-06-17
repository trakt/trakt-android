package tv.trakt.trakt.core.profile.sections.screentime.all

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.core.profile.sections.screentime.model.ScreenTimeData
import java.time.LocalDate

@Immutable
internal data class ScreenTimeAllState(
    val rangeStart: LocalDate? = null,
    val data: ScreenTimeData? = null,
)

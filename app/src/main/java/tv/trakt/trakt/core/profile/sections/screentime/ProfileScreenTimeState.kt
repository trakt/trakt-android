package tv.trakt.trakt.core.profile.sections.screentime

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.profile.sections.screentime.model.ScreenTimeData
import java.time.LocalDate

@Immutable
internal data class ProfileScreenTimeState(
    val rangeStart: LocalDate? = null,
    val data: ScreenTimeData? = null,
    val loading: LoadingState = LoadingState.Idle,
    val collapsed: Boolean? = null,
    val error: Exception? = null,
)

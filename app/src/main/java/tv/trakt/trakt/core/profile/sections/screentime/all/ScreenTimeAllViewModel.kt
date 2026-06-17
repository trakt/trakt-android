package tv.trakt.trakt.core.profile.sections.screentime.all

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.core.profile.sections.screentime.model.ScreenTimeData

internal class ScreenTimeAllViewModel(
    data: ScreenTimeData,
) : ViewModel() {
    val state = MutableStateFlow(
        ScreenTimeAllState(
            rangeStart = nowLocalDay().minusDays(6),
            data = data,
        ),
    ).asStateFlow()
}

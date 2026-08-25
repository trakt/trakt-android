package tv.trakt.trakt.ui.components.dateselection.otherdatepicker

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.home.sections.activity.model.HomeActivityItem

@Immutable
internal data class OtherDatePickerState(
    val items: ImmutableList<HomeActivityItem>? = null,
    val loading: LoadingState = LoadingState.Idle,
    val loadingMore: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
)

package tv.trakt.trakt.core.summary.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import tv.trakt.trakt.core.summary.social.model.MediaSocialActivity

internal class MediaSocialActivityViewModel(
    activity: ImmutableList<MediaSocialActivity>,
) : ViewModel() {
    private val activityState = MutableStateFlow(activity)

    val state = activityState
        .map { MediaSocialActivityState(activity = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MediaSocialActivityState(activity = activity),
        )
}

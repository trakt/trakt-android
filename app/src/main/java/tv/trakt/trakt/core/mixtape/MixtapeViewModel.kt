package tv.trakt.trakt.core.mixtape

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.core.filters.data.GlobalFilterManager

internal class MixtapeViewModel(
    filterManager: GlobalFilterManager,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = MixtapeState(
        type = filterManager.getFilter().mode.toMixtapeType(),
    )

    private val typeState = MutableStateFlow(initialState.type)

    init {
        analytics.logScreenView(
            screenName = "mixtape",
        )
    }

    private fun MediaMode.toMixtapeType(): MediaType {
        return when (this) {
            MediaMode.Shows -> MediaType.Show
            MediaMode.Movies, MediaMode.Media -> MediaType.Movie
        }
    }

    val state = typeState
        .map { MixtapeState(type = it) }
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5_000),
            initialValue = initialState,
        )
}

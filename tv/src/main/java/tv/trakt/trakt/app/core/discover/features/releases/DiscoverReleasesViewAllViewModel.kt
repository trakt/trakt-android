package tv.trakt.trakt.app.core.discover.features.releases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.app.core.discover.DiscoverConfig.DISCOVER_RELEASES_LIMIT
import tv.trakt.trakt.app.core.discover.DiscoverConfig.DISCOVER_RELEASES_RANGE
import tv.trakt.trakt.app.core.discover.usecase.GetDiscoverMediaUseCase
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.calendar.CalendarItem

@Suppress("UNCHECKED_CAST")
internal class DiscoverReleasesViewAllViewModel(
    private val getItemsUseCase: GetDiscoverMediaUseCase,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = DiscoverReleasesViewAllState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val itemsState = MutableStateFlow(initialState.items)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()

        analytics.logScreenView(
            screenName = "all_discover_releases",
        )
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { true }

                val items = getItemsUseCase.getReleases(
                    limit = DISCOVER_RELEASES_LIMIT,
                    range = DISCOVER_RELEASES_RANGE,
                )
                itemsState.update { items }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    val state = combine(
        loadingState,
        itemsState,
        errorState,
    ) { state ->
        DiscoverReleasesViewAllState(
            isLoading = state[0] as Boolean,
            items = state[1] as ImmutableList<CalendarItem>?,
            error = state[2] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

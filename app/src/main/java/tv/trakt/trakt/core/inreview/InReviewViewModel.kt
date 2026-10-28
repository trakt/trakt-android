package tv.trakt.trakt.core.inreview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.core.inreview.navigation.InReviewDestination

@Suppress("UNCHECKED_CAST")
internal class InReviewViewModel(
    savedStateHandle: SavedStateHandle,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<InReviewDestination>()
    private val initialState = InReviewState()

    private val urlState = MutableStateFlow(initialState.url)

    init {
        loadUrl()
    }

    private fun loadUrl() {
        viewModelScope.launch {
            val slurm = sessionManager.getProfile()?.settings?.slurm
            urlState.update {
                toInReviewWebUrl(
                    url = destination.url,
                    slurm = slurm,
                )
            }
        }
    }

    val state = combine(
        urlState,
    ) { state ->
        InReviewState(
            url = state[0] as String?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

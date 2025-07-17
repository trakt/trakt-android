package tv.trakt.trakt.tv.core.profile.sections.history

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.tv.core.profile.ProfileConfig.PROFILE_SECTION_LIMIT
import tv.trakt.trakt.tv.core.profile.sections.history.usecases.GetProfileHistoryUseCase
import tv.trakt.trakt.tv.core.profile.sections.history.usecases.SyncProfileHistoryUseCase
import tv.trakt.trakt.tv.helpers.extensions.nowUtc
import tv.trakt.trakt.tv.helpers.extensions.rethrowCancellation
import java.time.ZonedDateTime

internal class ProfileHistoryViewModel(
    private val getHistoryCase: GetProfileHistoryUseCase,
    private val syncHistoryCase: SyncProfileHistoryUseCase,
) : ViewModel() {
    private val initialState = ProfileHistoryState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadedAt: ZonedDateTime? = null

    init {
        loadData()
    }

    private fun loadData(showLoading: Boolean = true) {
        viewModelScope.launch {
            try {
                if (showLoading) {
                    loadingState.update { true }
                }

                itemsState.update {
                    getHistoryCase.getHistory(limit = PROFILE_SECTION_LIMIT)
                }

                loadedAt = nowUtc()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Log.e("ProfileHistoryViewModel", "Failed to load data", error)
                    errorState.update { error }
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    fun updateData() {
        Log.d("ProfileHistoryViewModel", "updateData called")
        viewModelScope.launch {
            try {
                if (syncHistoryCase.isSyncRequired(loadedAt)) {
                    Log.d("ProfileHistoryViewModel", "Sync needed, reloading data")
                    loadData(showLoading = false)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Log.e("ProfileHistoryViewModel", "Error", error)
                }
            }
        }
    }

    val state: StateFlow<ProfileHistoryState> = combine(
        loadingState,
        itemsState,
        errorState,
    ) { s1, s2, s3 ->
        ProfileHistoryState(
            isLoading = s1,
            items = s2,
            error = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

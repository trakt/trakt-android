package tv.trakt.trakt.tv.core.profile.sections.favorites.shows

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
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.tv.core.profile.ProfileConfig.PROFILE_FAVORITES_SECTION_LIMIT
import tv.trakt.trakt.tv.core.profile.sections.favorites.shows.usecases.GetFavoriteShowsUseCase
import tv.trakt.trakt.tv.helpers.extensions.nowUtc
import java.time.ZonedDateTime

internal class ProfileFavoriteShowsViewModel(
    private val getFavoriteShowsCase: GetFavoriteShowsUseCase,
) : ViewModel() {
    private val initialState = ProfileFavoriteShowsState()

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
                    getFavoriteShowsCase.getFavoriteShows(limit = PROFILE_FAVORITES_SECTION_LIMIT)
                }

                loadedAt = nowUtc()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Log.e("ProfileFavoriteShowsViewModel", "Failed to load data", error)
                    errorState.update { error }
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    val state: StateFlow<ProfileFavoriteShowsState> = combine(
        loadingState,
        itemsState,
        errorState,
    ) { s1, s2, s3 ->
        ProfileFavoriteShowsState(
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

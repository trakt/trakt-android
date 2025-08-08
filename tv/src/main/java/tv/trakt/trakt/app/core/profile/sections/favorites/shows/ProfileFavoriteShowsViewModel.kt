package tv.trakt.trakt.app.core.profile.sections.favorites.shows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.app.core.profile.ProfileConfig.PROFILE_FAVORITES_SECTION_LIMIT
import tv.trakt.trakt.app.core.profile.sections.favorites.shows.usecases.GetFavoriteShowsUseCase
import tv.trakt.trakt.app.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
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
                    Timber.e(error, "Failed to load data")
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

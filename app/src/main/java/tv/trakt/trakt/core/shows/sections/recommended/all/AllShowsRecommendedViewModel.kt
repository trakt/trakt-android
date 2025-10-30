package tv.trakt.trakt.core.shows.sections.recommended.all

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_BACKGROUND_IMAGE_URL
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.shows.sections.recommended.usecase.DEFAULT_ALL_LIMIT
import tv.trakt.trakt.core.shows.sections.recommended.usecase.GetRecommendedShowsUseCase

internal class AllShowsRecommendedViewModel(
    private val getRecommendedUseCase: GetRecommendedShowsUseCase,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = AllShowsRecommendedState()

    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadBackground()
        loadData()

        analytics.logScreenView(
            screenName = "AllShowsRecommended",
            screenClass = "AllShowsRecommendedScreen",
        )
    }

    private fun loadBackground() {
        val configUrl = Firebase.remoteConfig.getString(MOBILE_BACKGROUND_IMAGE_URL)
        backgroundState.update { configUrl }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val localShows = getRecommendedUseCase.getLocalShows()
                if (localShows.isNotEmpty()) {
                    itemsState.update {
                        localShows.toImmutableList()
                    }
                    loadingState.update { DONE }
                } else {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    getRecommendedUseCase.getShows(
                        limit = DEFAULT_ALL_LIMIT,
                    ).toImmutableList()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error, "Failed to load data")
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    val state: StateFlow<AllShowsRecommendedState> = combine(
        backgroundState,
        itemsState,
        loadingState,
        errorState,
    ) { s1, s2, s3, s4 ->
        AllShowsRecommendedState(
            backgroundUrl = s1,
            items = s2,
            loading = s3,
            error = s4,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

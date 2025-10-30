package tv.trakt.trakt.core.shows.sections.anticipated.all

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
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.core.shows.sections.anticipated.usecases.DEFAULT_ALL_LIMIT
import tv.trakt.trakt.core.shows.sections.anticipated.usecases.GetAnticipatedShowsUseCase

internal class AllShowsAnticipatedViewModel(
    private val getAnticipatedUseCase: GetAnticipatedShowsUseCase,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = AllShowsAnticipatedState()

    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(LoadingState.IDLE)
    private val errorState = MutableStateFlow(initialState.error)

    private var pages: Int = 1

    init {
        loadBackground()
        loadData()

        analytics.logScreenView(
            screenName = "AllShowsAnticipated",
        )
    }

    private fun loadBackground() {
        val configUrl = Firebase.remoteConfig.getString(MOBILE_BACKGROUND_IMAGE_URL)
        backgroundState.update { configUrl }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val localShows = getAnticipatedUseCase.getLocalShows()
                if (localShows.isNotEmpty()) {
                    itemsState.update {
                        localShows
                            .asyncMap { it.show }
                            .toImmutableList()
                    }
                    loadingState.update { DONE }
                } else {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    getAnticipatedUseCase.getShows(
                        page = 1,
                        limit = DEFAULT_ALL_LIMIT,
                    ).asyncMap {
                        it.show
                    }.toImmutableList()
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

    fun loadMoreData() {
        if (loadingMoreState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            try {
                loadingMoreState.update { LOADING }

                val nextData = getAnticipatedUseCase.getShows(
                    page = pages + 1,
                    limit = DEFAULT_ALL_LIMIT,
                    skipLocal = true,
                ).asyncMap {
                    it.show
                }

                itemsState.update {
                    it?.plus(nextData)?.toImmutableList()
                }

                pages += 1
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error, "Failed to load more page data")
                }
            } finally {
                loadingMoreState.update { DONE }
            }
        }
    }

    val state: StateFlow<AllShowsAnticipatedState> = combine(
        backgroundState,
        itemsState,
        loadingState,
        loadingMoreState,
        errorState,
    ) { s1, s2, s3, s4, s5 ->
        AllShowsAnticipatedState(
            backgroundUrl = s1,
            items = s2,
            loading = s3,
            loadingMore = s4,
            error = s5,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

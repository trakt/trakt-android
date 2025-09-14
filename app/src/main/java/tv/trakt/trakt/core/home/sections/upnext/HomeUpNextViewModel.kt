package tv.trakt.trakt.core.home.sections.upnext

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.IDLE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.home.sections.upnext.HomeUpNextState.ItemsState
import tv.trakt.trakt.core.home.sections.upnext.model.ProgressShow
import tv.trakt.trakt.core.home.sections.upnext.usecases.GetUpNextUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.resources.R
import java.time.Instant

internal class HomeUpNextViewModel(
    private val getUpNextUseCase: GetUpNextUseCase,
    private val updateHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = HomeUpNextState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)

    private var user: User? = null
    private var loadedAt: Instant? = null
    private var processingJob: kotlinx.coroutines.Job? = null

    init {
        loadData()
        observeUser()
    }

    private fun observeUser() {
        viewModelScope.launch {
            user = sessionManager.getProfile()
            sessionManager.observeProfile()
                .collect {
                    if (user != it) {
                        user = it
                        loadData()
                    }
                }
        }
    }

    private fun loadData(resetScroll: Boolean = true) {
        viewModelScope.launch {
            if (loadEmptyIfNeeded()) {
                return@launch
            }

            try {
                val localItems = getUpNextUseCase.getLocalUpNext()
                if (localItems.isNotEmpty()) {
                    itemsState.update {
                        ItemsState(
                            items = localItems,
                            resetScroll = false,
                        )
                    }
                    loadingState.update { DONE }
                } else {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    ItemsState(
                        items = getUpNextUseCase.getUpNext(),
                        resetScroll = resetScroll,
                    )
                }

                loadedAt = nowUtcInstant()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.d(error, "Failed to load data")
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update {
                ItemsState(emptyList<ProgressShow>().toImmutableList())
            }
            loadingState.update { DONE }
            return true
        } else {
            itemsState.update { ItemsState() }
            loadingState.update { IDLE }
        }

        return false
    }

    fun addToHistory(episodeId: TraktId) {
        if (processingJob != null) {
            return
        }
        processingJob = viewModelScope.launch {
            try {
                val currentItems = itemsState.value.items?.toMutableList() ?: return@launch

                val itemIndex = currentItems.indexOfFirst { it.id == episodeId }
                val itemLoading = currentItems[itemIndex].copy(loading = true)
                currentItems[itemIndex] = itemLoading

                itemsState.update {
                    ItemsState(
                        items = currentItems.toImmutableList(),
                        resetScroll = false,
                    )
                }

                updateHistoryUseCase.addToHistory(episodeId)
                itemsState.update {
                    ItemsState(
                        items = getUpNextUseCase.getUpNext(),
                        resetScroll = false,
                    )
                }
                infoState.update {
                    DynamicStringResource(R.string.text_info_history_added)
                }

                loadedAt = nowUtcInstant()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.d(error, "Failed to add movie to history")
                }
            } finally {
                processingJob = null
            }
        }
    }

    fun clearInfo() {
        infoState.update { null }
    }

    val state: StateFlow<HomeUpNextState> = combine(
        loadingState,
        itemsState,
        infoState,
        errorState,
    ) { s1, s2, s3, s4 ->
        HomeUpNextState(
            loading = s1,
            items = s2,
            info = s3,
            error = s4,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

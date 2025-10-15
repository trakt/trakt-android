package tv.trakt.trakt.core.summary.shows.features.seasons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.summary.shows.features.seasons.model.ShowSeasons
import tv.trakt.trakt.core.summary.shows.features.seasons.usecases.GetShowSeasonsUseCase

internal class ShowSeasonsViewModel(
    private val show: Show,
    private val getSeasonsUseCase: GetShowSeasonsUseCase,
) : ViewModel() {
    private val initialState = ShowSeasonsState()

    private val showState = MutableStateFlow(show)
    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadingJob: Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { LOADING }
                itemsState.update {
                    getSeasonsUseCase.getAllSeasons(show.ids.trakt)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w(error)
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    fun loadSeason(season: Season) {
        loadingJob = viewModelScope.launch {
            delay(250)
            itemsState.update {
                it.copy(isSeasonLoading = true)
            }
        }

        if (itemsState.value.isSeasonLoading ||
            season.number == itemsState.value.selectedSeason?.number
        ) {
            loadingJob?.cancel()
            return
        }

        viewModelScope.launch {
            try {
                itemsState.update {
                    it.copy(selectedSeason = season)
                }

                val episodes = getSeasonsUseCase.getSeasonEpisodes(
                    showId = show.ids.trakt,
                    season = season.number,
                )

                itemsState.update {
                    it.copy(
                        selectedSeason = season,
                        selectedSeasonEpisodes = episodes,
                        isSeasonLoading = false,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w("Error loading season: ${error.message}")
                    itemsState.update { it.copy(isSeasonLoading = false) }
                }
            } finally {
                loadingJob?.cancel()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<ShowSeasonsState> = combine(
        showState,
        itemsState,
        loadingState,
        errorState,
    ) { state ->
        ShowSeasonsState(
            show = state[0] as Show,
            items = state[1] as ShowSeasons,
            loading = state[2] as LoadingState,
            error = state[3] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

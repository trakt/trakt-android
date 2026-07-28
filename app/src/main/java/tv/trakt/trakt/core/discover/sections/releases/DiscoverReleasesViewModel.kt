@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.discover.sections.releases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.extensions.toLocalDay
import tv.trakt.trakt.common.model.MediaMode.Media
import tv.trakt.trakt.common.model.MediaMode.Movies
import tv.trakt.trakt.common.model.MediaMode.Shows
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.calendar.model.CalendarItem
import tv.trakt.trakt.core.discover.sections.releases.usecases.GetReleasesTypeUseCase
import tv.trakt.trakt.core.discover.sections.releases.usecases.movies.GetReleasesMoviesUseCase
import tv.trakt.trakt.core.discover.sections.releases.usecases.shows.GetReleasesShowsUseCase
import tv.trakt.trakt.core.discover.sections.releases.usecases.shows.ReleaseType
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey
import java.time.LocalDate
import java.time.temporal.ChronoUnit.DAYS

internal class DiscoverReleasesViewModel(
    private val filterManager: GlobalFilterManager,
    private val getReleasesShowsUseCase: GetReleasesShowsUseCase,
    private val getReleasesMoviesUseCase: GetReleasesMoviesUseCase,
    private val getReleasesTypeUseCase: GetReleasesTypeUseCase,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = DiscoverReleasesState()

    private val filterState = MutableStateFlow(filterManager.getFilter())
    private val typeState = MutableStateFlow(ReleaseType.All)
    private val collapseState = MutableStateFlow(isCollapsed())
    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null
    private var collapseJob: Job? = null

    init {
        loadData()
        observeMode()
    }

    private fun observeMode() {
        filterManager.observeFilter()
            .onEach { value ->
                filterState.update { value }
                collapseState.update { isCollapsed() }
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                loadType()
                loadLocalData()
                loadRemoteData()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
                dataJob = null
            }
        }
    }

    private suspend fun loadType() {
        typeState.update { getReleasesTypeUseCase.getType() }
    }

    private suspend fun loadLocalData() {
        return coroutineScope {
            val startDate = nowUtcInstant()
            val startDay = startDate.toLocalDay()

            val localShowsAsync = async { getReleasesShowsUseCase.getLocalShows() }
            val localMoviesAsync = async { getReleasesMoviesUseCase.getLocalMovies() }

            val localShows = if (filterState.value.mode.isMediaOrShows) localShowsAsync.await() else emptyList()
            val localMovies = if (filterState.value.mode.isMediaOrMovies) localMoviesAsync.await() else emptyList()

            if (localShows.isNotEmpty() || localMovies.isNotEmpty()) {
                itemsState.update {
                    (localShows + localMovies)
                        .filter {
                            val releaseLocal = it.releasedAt?.toLocalDay() ?: LocalDate.MIN
                            releaseLocal >= startDay
                        }
                        .sortedBy { it.releasedAt }
                        .toImmutableList()
                }
                loadingState.update { Done }
            } else {
                loadingState.update { Loading }
            }
        }
    }

    private suspend fun loadRemoteData() {
        return coroutineScope {
            val startDate = nowUtcInstant()
            val startDay = startDate.toLocalDay()

            val showsAsync = async {
                getReleasesShowsUseCase.getShows(
                    startDate = startDate.minus(1, DAYS),
                    days = 14,
                    filters = filterState.value,
                    type = typeState.value,
                )
            }
            val moviesAsync = async {
                getReleasesMoviesUseCase.getMovies(
                    startDate = startDate.minus(1, DAYS),
                    days = 14,
                    filters = filterState.value,
                )
            }

            val shows = if (filterState.value.mode.isMediaOrShows) showsAsync.await() else emptyList()
            val movies = if (typeState.value != ReleaseType.Finale && filterState.value.mode.isMediaOrMovies) {
                moviesAsync.await()
            } else {
                emptyList()
            }

            itemsState.update {
                (shows + movies)
                    .filter {
                        val releaseLocal = it.releasedAt?.toLocalDay() ?: LocalDate.MIN
                        releaseLocal >= startDay
                    }
                    .sortedBy { it.releasedAt }
                    .toImmutableList()
            }
        }
    }

    fun setType(type: ReleaseType) {
        if (type == typeState.value || loadingState.value.isLoading) {
            return
        }
        viewModelScope.launch {
            getReleasesTypeUseCase.setType(type)
            // Local caches are not release-type aware, so clear them on change to
            // avoid briefly serving stale items from the previously selected type.
            getReleasesShowsUseCase.clearLocal()
            getReleasesMoviesUseCase.clearLocal()
            loadData()
        }
    }

    fun setCollapsed(collapsed: Boolean) {
        collapseState.update { collapsed }

        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            val key = when (filterState.value.mode) {
                Media -> CollapsingKey.DISCOVER_MEDIA_RELEASES
                Shows -> CollapsingKey.DISCOVER_SHOWS_RELEASES
                Movies -> CollapsingKey.DISCOVER_MOVIES_RELEASES
            }
            when {
                collapsed -> collapsingManager.collapse(key)
                else -> collapsingManager.expand(key)
            }
        }
    }

    private fun isCollapsed(): Boolean {
        return collapsingManager.isCollapsed(
            key = when (filterState.value.mode) {
                Media -> CollapsingKey.DISCOVER_MEDIA_RELEASES
                Shows -> CollapsingKey.DISCOVER_SHOWS_RELEASES
                Movies -> CollapsingKey.DISCOVER_MOVIES_RELEASES
            },
        )
    }

    val state = combine(
        itemsState,
        filterState,
        typeState,
        collapseState,
        loadingState,
        errorState,
    ) { state ->
        DiscoverReleasesState(
            items = state[0] as ImmutableList<CalendarItem>?,
            filter = state[1] as GlobalFilter?,
            type = state[2] as ReleaseType,
            collapsed = state[3] as Boolean,
            loading = state[4] as LoadingState,
            error = state[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

package tv.trakt.trakt.core.discover.sections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider
import tv.trakt.trakt.common.helpers.lifecycle.observeForegroundRefresh
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.MediaMode.Media
import tv.trakt.trakt.common.model.MediaMode.Movies
import tv.trakt.trakt.common.model.MediaMode.Shows
import tv.trakt.trakt.common.model.discover.DiscoverItem
import tv.trakt.trakt.common.model.discover.DiscoverSection
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.core.discover.DiscoverConfig.DEFAULT_SECTION_LIMIT
import tv.trakt.trakt.core.discover.sections.all.usecases.GetAllDiscoverMoviesUseCase
import tv.trakt.trakt.core.discover.sections.all.usecases.GetAllDiscoverShowsUseCase
import tv.trakt.trakt.core.discover.usecases.GetDiscoverMediaUseCase
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.settings.usecases.SeasonalThemeUseCase
import tv.trakt.trakt.helpers.collapsing.CollapseDelegate
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

@Suppress("UNCHECKED_CAST")
internal class DiscoverSectionViewModel(
    private val section: DiscoverSection,
    private val filterManager: GlobalFilterManager,
    private val getShowsUseCase: GetAllDiscoverShowsUseCase,
    private val getMoviesUseCase: GetAllDiscoverMoviesUseCase,
    private val getDiscoverMediaUseCase: GetDiscoverMediaUseCase,
    private val seasonalThemeUseCase: SeasonalThemeUseCase,
    private val appLifecycleProvider: AppLifecycleProvider,
    collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = DiscoverSectionState()

    private val filterState = MutableStateFlow(filterManager.getFilter())
    private val collapse = CollapseDelegate(
        manager = collapsingManager,
        scope = viewModelScope,
        key = { section.collapsingKey(filterState.value.mode) },
    )
    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null

    init {
        loadData()
        observeMode()
        observeSeasonalTheme()
        observeForeground()
    }

    private fun observeForeground() {
        appLifecycleProvider.observeForegroundRefresh()
            .onEach { loadData(ignoreErrors = true) }
            .launchIn(viewModelScope)
    }

    private fun observeMode() {
        filterManager.observeFilter()
            .onEach { value ->
                filterState.update { value }
                collapse.refresh()
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private fun observeSeasonalTheme() {
        seasonalThemeUseCase.observeActive()
            .distinctUntilChanged()
            .drop(1)
            .onEach { loadData() }
            .launchIn(viewModelScope)
    }

    private fun loadData(ignoreErrors: Boolean = false) {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                loadLocalData()
                loadRemoteData()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
                dataJob = null
            }
        }
    }

    private suspend fun loadLocalData() {
        val localItems = when (filterState.value.mode) {
            Media -> getDiscoverMediaUseCase.getLocalMedia(section)
            Shows -> getShowsUseCase.getLocalShows(section)
            Movies -> getMoviesUseCase.getLocalMovies(section)
        }

        if (localItems.isNotEmpty()) {
            itemsState.update { localItems }
            loadingState.update { Done }
        } else {
            loadingState.update { Loading }
        }
    }

    private suspend fun loadRemoteData() {
        val filter = filterState.value
        val items = when (filter.mode) {
            Media -> getDiscoverMediaUseCase.getMedia(
                section = section,
                filters = filter,
            )

            Shows -> getShowsUseCase.getShows(
                source = section,
                limit = DEFAULT_SECTION_LIMIT,
                filters = filter,
            )

            Movies -> getMoviesUseCase.getMovies(
                source = section,
                limit = DEFAULT_SECTION_LIMIT,
                filters = filter,
            )
        }

        itemsState.update { items }
    }

    fun setCollapsed(collapsed: Boolean) {
        collapse.set(collapsed)
    }

    val state = combine(
        itemsState,
        filterState,
        collapse.state,
        loadingState,
        errorState,
    ) { state ->
        DiscoverSectionState(
            items = state[0] as ImmutableList<DiscoverItem>?,
            filter = state[1] as GlobalFilter?,
            collapsed = state[2] as Boolean,
            loading = state[3] as LoadingState,
            error = state[4] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

private fun DiscoverSection.collapsingKey(mode: MediaMode): CollapsingKey {
    return when (this) {
        DiscoverSection.Trending -> when (mode) {
            Media -> CollapsingKey.DISCOVER_MEDIA_TRENDING
            Shows -> CollapsingKey.DISCOVER_SHOWS_TRENDING
            Movies -> CollapsingKey.DISCOVER_MOVIES_TRENDING
        }

        DiscoverSection.Popular -> when (mode) {
            Media -> CollapsingKey.DISCOVER_MEDIA_POPULAR
            Shows -> CollapsingKey.DISCOVER_SHOWS_POPULAR
            Movies -> CollapsingKey.DISCOVER_MOVIES_POPULAR
        }

        DiscoverSection.Anticipated -> when (mode) {
            Media -> CollapsingKey.DISCOVER_MEDIA_ANTICIPATED
            Shows -> CollapsingKey.DISCOVER_SHOWS_ANTICIPATED
            Movies -> CollapsingKey.DISCOVER_MOVIES_ANTICIPATED
        }

        DiscoverSection.Recommended -> when (mode) {
            Media -> CollapsingKey.DISCOVER_MEDIA_RECOMMENDED
            Shows -> CollapsingKey.DISCOVER_SHOWS_RECOMMENDED
            Movies -> CollapsingKey.DISCOVER_MOVIES_RECOMMENDED
        }
    }
}

package tv.trakt.trakt.core.lists.sections.liked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_ITEMS_SECTION_LIMIT
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.lists.sections.liked.usecases.GetLikedListItemsUseCase
import tv.trakt.trakt.core.lists.sections.liked.usecases.GetLikedListsUseCase
import tv.trakt.trakt.core.user.CollectionStateProvider
import tv.trakt.trakt.core.user.UserCollectionState
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

@OptIn(FlowPreview::class)
internal class ListsLikedViewModel(
    private val listId: TraktId,
    private val getLikedListUseCase: GetLikedListsUseCase,
    private val getLikedListItemsUseCase: GetLikedListItemsUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val episodeLocalDataSource: EpisodeLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val collectionStateProvider: CollectionStateProvider,
    private val filterManager: GlobalFilterManager,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = ListsLikedState()
    private val initialMode = filterManager.getFilter()

    private val userState = MutableStateFlow(initialState.user)
    private val filterState = MutableStateFlow(initialMode)
    private val collapseState = MutableStateFlow(isCollapsed())
    private val listState = MutableStateFlow(initialState.list)
    private val itemsState = MutableStateFlow(initialState.items)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val navigateEpisode = MutableStateFlow(initialState.navigateEpisode)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null
    private var processingJob: Job? = null
    private var collapseJob: Job? = null

    init {
        loadData()

        observeMode()
        observeCollection()
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

    private fun observeCollection() {
        collectionStateProvider
            .launchIn(viewModelScope)
    }

    private fun loadData(ignoreErrors: Boolean = false) {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                listState.update {
                    getLikedListUseCase.getLocalList(listId)
                }

                val localItems = getLikedListItemsUseCase.getLocalItems(
                    listId = listId,
                    filter = filterState.value.mode,
                )

                if (localItems.isNotEmpty()) {
                    itemsState.update { localItems }
                    loadingState.update { Done }
                } else {
                    loadingState.update { Loading }
                }

                itemsState.update {
                    getLikedListItemsUseCase.getItems(
                        listId = listId,
                        limit = LISTS_ITEMS_SECTION_LIMIT,
                        filter = filterState.value.mode,
                        sorting = Sorting.Default,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                }
            } finally {
                loadingState.update { Done }
                dataJob = null
            }
        }
    }

    fun navigateToMovie(movie: Movie) {
        if (navigateMovie.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            movieLocalDataSource.upsertMovies(listOf(movie))
            navigateMovie.update { movie.ids.trakt }
        }
    }

    fun navigateToShow(show: Show) {
        if (navigateShow.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            showLocalDataSource.upsertShows(listOf(show))
            navigateShow.update { show.ids.trakt }
        }
    }

    fun navigateToEpisode(
        show: Show,
        episode: Episode,
    ) {
        if (navigateEpisode.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            showLocalDataSource.upsertShows(listOf(show))
            episodeLocalDataSource.upsertEpisodes(listOf(episode))

            navigateEpisode.update {
                Pair(show.ids.trakt, episode)
            }
        }
    }

    fun clearNavigation() {
        navigateShow.update { null }
        navigateMovie.update { null }
        navigateEpisode.update { null }
    }

    fun setCollapsed(collapsed: Boolean) {
        collapseState.update { collapsed }

        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            val key = when (filterState.value.mode) {
                MediaMode.MEDIA -> CollapsingKey.LISTS_MEDIA_LIKED
                MediaMode.SHOWS -> CollapsingKey.LISTS_SHOWS_LIKED
                MediaMode.MOVIES -> CollapsingKey.LISTS_MOVIES_LIKED
            }
            when {
                collapsed -> collapsingManager.collapse("${key.preferenceKey}-${listId.value}")
                else -> collapsingManager.expand("${key.preferenceKey}-${listId.value}")
            }
        }
    }

    private fun isCollapsed(): Boolean {
        val key = when (filterState.value.mode) {
            MediaMode.MEDIA -> CollapsingKey.LISTS_MEDIA_LIKED
            MediaMode.SHOWS -> CollapsingKey.LISTS_SHOWS_LIKED
            MediaMode.MOVIES -> CollapsingKey.LISTS_MOVIES_LIKED
        }
        return collapsingManager
            .isCollapsed("${key.preferenceKey}-${listId.value}")
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        listState,
        userState,
        itemsState,
        filterState,
        collapseState,
        collectionStateProvider.stateFlow,
        navigateShow,
        navigateMovie,
        navigateEpisode,
        loadingState,
        errorState,
    ) { states ->
        ListsLikedState(
            list = states[0] as CustomList?,
            user = states[1] as User?,
            items = states[2] as ImmutableList<CustomListItem>?,
            filter = states[3] as GlobalFilter?,
            collapsed = states[4] as Boolean,
            collection = states[5] as UserCollectionState,
            navigateShow = states[6] as TraktId?,
            navigateMovie = states[7] as TraktId?,
            navigateEpisode = states[8] as Pair<TraktId, Episode>?,
            loading = states[9] as LoadingState,
            error = states[10] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

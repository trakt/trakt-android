package tv.trakt.trakt.core.lists.features.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserLikedListsUseCase
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_ITEMS_ALL_LIMIT
import tv.trakt.trakt.core.lists.features.details.ListDetailsState.LikedInfo
import tv.trakt.trakt.core.lists.features.details.ListDetailsState.ListDetails
import tv.trakt.trakt.core.lists.features.details.navigation.ListsDetailsDestination
import tv.trakt.trakt.core.lists.features.details.usecases.GetListItemsUseCase
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.lists.sections.liked.usecases.manage.AddLikedListUseCase
import tv.trakt.trakt.core.lists.sections.liked.usecases.manage.RemoveLikedListUseCase
import tv.trakt.trakt.core.user.CollectionStateProvider
import tv.trakt.trakt.core.user.UserCollectionState
import tv.trakt.trakt.resources.R

private const val PAGE_LIMIT = LISTS_ITEMS_ALL_LIMIT

@OptIn(FlowPreview::class)
internal class ListDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    filtersManager: GlobalFilterManager,
    private val getListItemsUseCase: GetListItemsUseCase,
    private val getListLikedUseCase: LoadUserLikedListsUseCase,
    private val addLikedListUseCase: AddLikedListUseCase,
    private val removeLikedListUseCase: RemoveLikedListUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val episodeLocalDataSource: EpisodeLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val collectionStateProvider: CollectionStateProvider,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<ListsDetailsDestination>()
    private val destinationList = Json.decodeFromString<CustomList>(destination.listJson)

    private val showFilters = destination.mediaType.size > 1

    private val initialState = ListDetailsState()
    private val listState = MutableStateFlow(
        ListDetails(
            list = destinationList,
            mediaId = destination.mediaId.toTraktId(),
        ),
    )
    private val itemsState = MutableStateFlow(initialState.items)
    private val likedState = MutableStateFlow(initialState.liked)
    private val filterState = MutableStateFlow(
        when {
            showFilters -> filtersManager.getFilter()
            else -> null
        },
    )
    private val sortingState = MutableStateFlow(initialState.sorting)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val navigateEpisode = MutableStateFlow(initialState.navigateEpisode)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(initialState.loadingMore)
    private val userState = MutableStateFlow(initialState.user)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)

    private var dataJob: Job? = null
    private var processingJob: Job? = null

    private var page: Int = 1
    private var hasMoreData: Boolean = false

    init {
        loadUser()
        loadInitialData()
        loadLikedData()

        observeCollection()
    }

    private fun observeCollection() {
        collectionStateProvider
            .launchIn(viewModelScope)
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                userState.update {
                    sessionManager.getProfile()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun loadLikedData() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }

            try {
                val likedLists = getListLikedUseCase.loadIfNeeded()
                likedState.update {
                    LikedInfo(
                        liked = likedLists.containsKey(destinationList.ids.trakt),
                    )
                }
            } catch (error: Exception) {
                Timber.recordError(error)
            }
        }
    }

    fun loadInitialData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                if (loadEmptyIfNeeded()) return@launch
                loadingState.update { Loading }

                val items = getListItemsUseCase.getItems(
                    listId = destinationList.ids.trakt,
                    type = filterState.value?.mode.toMediaTypes(),
                    sorting = sortingState.value,
                    pagination = Pagination(1, PAGE_LIMIT),
                    filters = filterState.value,
                )
                    .distinctBy { it.key }
                    .toImmutableList()

                itemsState
                    .update { items }
                    .also {
                        hasMoreData = items.size >= PAGE_LIMIT
                    }
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

    fun loadData(ignoreErrors: Boolean = false) {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                page = 1
                hasMoreData = true

                loadingState.update { Loading }

                val items = getListItemsUseCase.getItems(
                    listId = destinationList.ids.trakt,
                    type = filterState.value?.mode.toMediaTypes(),
                    sorting = sortingState.value,
                    pagination = Pagination(1, PAGE_LIMIT),
                    filters = filterState.value,
                )
                    .distinctBy { it.key }
                    .toImmutableList()

                itemsState.update {
                    items
                }.also {
                    hasMoreData = items.size >= PAGE_LIMIT
                }
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

    private suspend fun loadEmptyIfNeeded(): Boolean {
        if (!sessionManager.isAuthenticated()) {
            itemsState.update { EmptyImmutableList }
            loadingState.update { Done }
            return true
        } else {
            itemsState.update { null }
            loadingState.update { Idle }
        }

        return false
    }

    fun loadMoreData() {
        if (itemsState.value.isNullOrEmpty() || !hasMoreData) {
            return
        }
        if (loadingMoreState.value.isLoading || loadingState.value.isLoading) {
            return
        }

        dataJob = viewModelScope.launch {
            try {
                loadingMoreState.update { Loading }

                val nextPage = page + 1
                val nextItems = getListItemsUseCase.getItems(
                    listId = destinationList.ids.trakt,
                    type = filterState.value?.mode.toMediaTypes(),
                    filters = filterState.value,
                    sorting = sortingState.value,
                    pagination = Pagination(nextPage, PAGE_LIMIT),
                )

                itemsState.update { items ->
                    items
                        ?.plus(nextItems)
                        ?.distinctBy { it.key }
                        ?.toImmutableList()
                }

                page = nextPage
                hasMoreData = (nextItems.size >= PAGE_LIMIT)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            } finally {
                loadingMoreState.update { Done }
                dataJob = null
            }
        }
    }

    fun setSorting(newSorting: Sorting) {
        if (newSorting == sortingState.value || loadingState.value.isLoading) {
            return
        }

        sortingState.update {
            it.copy(
                type = newSorting.type,
                order = newSorting.order,
            )
        }

        loadData()
    }

    fun setFilter(newFilter: GlobalFilter) {
        if (newFilter == filterState.value) {
            return
        }
        filterState.update { newFilter }
        loadData()
    }

    fun setLiked(liked: Boolean) {
        if (likedState.value?.liked == liked || likedState.value?.loading == true) {
            return
        }

        viewModelScope.launch {
            try {
                likedState.update { it?.copy(loading = true) }

                if (liked) {
                    addLikedListUseCase.addToLiked(destinationList)
                    infoState.update { DynamicStringResource(R.string.text_info_liked_added) }
                } else {
                    removeLikedListUseCase.removeFromLiked(destinationList.ids.trakt)
                    infoState.update { DynamicStringResource(R.string.text_info_liked_removed) }
                }

                likedState.update { LikedInfo(liked = liked) }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    likedState.update { it?.copy(loading = false) }
                    Timber.recordError(error)
                }
            }
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

    fun navigateToMovie(movie: Movie) {
        if (navigateMovie.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            movieLocalDataSource.upsertMovies(listOf(movie))
            navigateMovie.update { movie.ids.trakt }
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

    fun clearInfoError() {
        infoState.update { null }
        errorState.update { null }
    }

    override fun onCleared() {
        dataJob?.cancel()
        processingJob?.cancel()
        super.onCleared()
    }

    private fun MediaMode?.toMediaTypes(): List<MediaType> {
        return when (this) {
            MediaMode.SHOWS -> listOf(MediaType.SHOW)
            MediaMode.MOVIES -> listOf(MediaType.MOVIE)
            else -> destination.mediaType.map { MediaType.valueOf(it) }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        loadingState,
        loadingMoreState,
        listState,
        itemsState,
        filterState,
        sortingState,
        likedState,
        collectionStateProvider.stateFlow,
        navigateShow,
        navigateMovie,
        navigateEpisode,
        userState,
        infoState,
        errorState,
    ) { state ->
        ListDetailsState(
            loading = state[0] as LoadingState,
            loadingMore = state[1] as LoadingState,
            list = state[2] as ListDetails,
            items = state[3] as ImmutableList<CustomListItem>?,
            filter = state[4] as GlobalFilter?,
            sorting = state[5] as Sorting,
            liked = state[6] as LikedInfo?,
            collection = state[7] as UserCollectionState,
            navigateShow = state[8] as TraktId?,
            navigateMovie = state[9] as TraktId?,
            navigateEpisode = state[10] as Pair<TraktId, Episode>?,
            user = state[11] as User?,
            info = state[12] as StringResource?,
            error = state[13] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

package tv.trakt.trakt.app.core.details.lists.details.media

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.app.core.details.lists.details.CustomListDetailsConfig.CUSTOM_LIST_PAGE_LIMIT
import tv.trakt.trakt.app.core.details.lists.details.media.navigation.CustomListMediaDestination
import tv.trakt.trakt.app.core.details.lists.details.media.usecases.GetListItemsUseCase
import tv.trakt.trakt.app.core.lists.filters.TvListFilterConfiguration
import tv.trakt.trakt.app.core.lists.filters.TvListRequest
import tv.trakt.trakt.app.core.lists.usecases.liked.AddLikedListUseCase
import tv.trakt.trakt.app.core.lists.usecases.liked.RemoveLikedListUseCase
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserLikedListsUseCase
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.globalfilter.GlobalFilter
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.resources.R

internal class CustomListMediaViewModel(
    savedStateHandle: SavedStateHandle,
    private val getListItemsUseCase: GetListItemsUseCase,
    private val getUserLikedListsUseCase: LoadUserLikedListsUseCase,
    private val addLikedListUseCase: AddLikedListUseCase,
    private val removeLikedListUseCase: RemoveLikedListUseCase,
    private val collectionStateProvider: CollectionStateProvider,
) : ViewModel() {
    val destination = savedStateHandle.toRoute<CustomListMediaDestination>()
    private val destinationType = MediaType.entries.find { it.value == destination.listType }
    val filterConfiguration = when (destinationType) {
        MediaType.Show -> TvListFilterConfiguration.ShowsList
        MediaType.Movie -> TvListFilterConfiguration.MoviesList
        MediaType.Season,
        MediaType.Episode,
        null,
        -> TvListFilterConfiguration.MixedList
    }

    private val _state = MutableStateFlow(
        CustomListMediaState(
            like = CustomListMediaState.LikedState(
                likesCount = destination.listLikes,
            ),
            filter = filterConfiguration.defaultFilter,
        ),
    )
    val state = _state.asStateFlow()

    private var requestJob: Job? = null
    private var nextDataPage = 1
    private var hasMoreData = true

    init {
        observeCollection()
        reload()
        loadLikeData()
    }

    fun applyFilter(filter: GlobalFilter) {
        _state.update {
            it.copy(filter = filterConfiguration.normalize(filter))
        }
        reload()
    }

    fun applySorting(sorting: Sorting) {
        _state.update {
            it.copy(sorting = sorting)
        }
        reload()
    }

    private fun observeCollection() {
        collectionStateProvider.launchIn(viewModelScope)
        collectionStateProvider.stateFlow
            .onEach { collection ->
                _state.update { it.copy(collection = collection) }
            }
            .launchIn(viewModelScope)
    }

    private fun reload() {
        requestJob?.cancel()
        nextDataPage = 1
        hasMoreData = true
        _state.update {
            it.copy(
                isLoading = true,
                isLoadingPage = false,
                error = null,
            )
        }

        val request = _state.value.toRequest(page = 1)
        requestJob = viewModelScope.launch {
            try {
                val page = getListItemsUseCase.getListItems(
                    listId = destination.listId.toTraktId(),
                    request = request,
                )
                nextDataPage = page.nextPage
                hasMoreData = page.hasMore
                _state.update {
                    it.copy(
                        isLoading = false,
                        items = page.items,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error,
                        )
                    }
                    Timber.e(error, "Error loading media for list: %s", destination.listId)
                }
            }
        }
    }

    fun loadMoreData() {
        val currentState = _state.value
        if (currentState.isLoading || currentState.isLoadingPage || !hasMoreData) return

        _state.update {
            it.copy(
                isLoadingPage = true,
                error = null,
            )
        }
        val request = _state.value.toRequest(page = nextDataPage)
        requestJob = viewModelScope.launch {
            try {
                val page = getListItemsUseCase.getListItems(
                    listId = destination.listId.toTraktId(),
                    request = request,
                )
                nextDataPage = page.nextPage
                hasMoreData = page.hasMore
                _state.update { state ->
                    state.copy(
                        isLoadingPage = false,
                        items = (
                            state.items.orEmpty() + page.items
                        ).distinctBy { it.key }
                            .toImmutableList(),
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    _state.update {
                        it.copy(
                            isLoadingPage = false,
                            error = error,
                        )
                    }
                    Timber.e(error, "Error loading next media page for list: %s", destination.listId)
                }
            }
        }
    }

    private fun loadLikeData() {
        viewModelScope.launch {
            _state.update {
                it.copy(like = it.like.copy(isLoading = true))
            }
            try {
                val likedLists = getUserLikedListsUseCase.loadIfNeeded()
                _state.update {
                    it.copy(
                        like = it.like.copy(
                            isLiked = likedLists.containsKey(destination.listId.toTraktId()),
                            isLoading = false,
                        ),
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    _state.update {
                        it.copy(like = it.like.copy(isLoading = false))
                    }
                    Timber.e(error, "Error loading like state for list: %s", destination.listId)
                }
            }
        }
    }

    fun setLiked(liked: Boolean) {
        val currentLike = _state.value.like
        if (currentLike.isLiked == liked || currentLike.isLoading) return

        viewModelScope.launch {
            _state.update {
                it.copy(like = it.like.copy(isLoading = true))
            }
            try {
                val traktId = destination.listId.toTraktId()
                if (liked) {
                    addLikedListUseCase.addToLiked(traktId)
                } else {
                    removeLikedListUseCase.removeFromLiked(traktId)
                }

                _state.update {
                    val likesCount = when (liked) {
                        true -> it.like.likesCount + 1
                        false -> (it.like.likesCount - 1).coerceAtLeast(0)
                    }
                    it.copy(
                        like = it.like.copy(
                            likesCount = likesCount,
                            isLiked = liked,
                            isLoading = false,
                        ),
                        info = DynamicStringResource(
                            when (liked) {
                                true -> R.string.text_info_liked_added
                                false -> R.string.text_info_liked_removed
                            },
                        ),
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    _state.update {
                        it.copy(
                            like = it.like.copy(isLoading = false),
                            error = error,
                        )
                    }
                }
            }
        }
    }

    fun clearInfo() {
        _state.update { it.copy(info = null) }
    }

    private fun CustomListMediaState.toRequest(page: Int): TvListRequest {
        return TvListRequest(
            page = page,
            limit = CUSTOM_LIST_PAGE_LIMIT,
            filter = filter,
            sorting = sorting,
        )
    }
}

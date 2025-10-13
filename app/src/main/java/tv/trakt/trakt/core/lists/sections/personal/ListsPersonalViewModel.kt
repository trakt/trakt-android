package tv.trakt.trakt.core.lists.sections.personal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.lists.ListsConfig.LISTS_SECTION_LIMIT
import tv.trakt.trakt.core.lists.model.PersonalListItem
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalItemsLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.data.local.ListsPersonalLocalDataSource
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListItemsUseCase
import tv.trakt.trakt.core.lists.sections.personal.usecases.GetPersonalListsUseCase

@OptIn(FlowPreview::class)
internal class ListsPersonalViewModel(
    private val listId: TraktId,
    private val getListUseCase: GetPersonalListsUseCase,
    private val getListItemsUseCase: GetPersonalListItemsUseCase,
    private val localListsSource: ListsPersonalLocalDataSource,
    private val localListsItemsSource: ListsPersonalItemsLocalDataSource,
    private val showLocalDataSource: ShowLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
) : ViewModel() {
    private val initialState = ListsPersonalState()

    private val userState = MutableStateFlow(initialState.user)
    private val listState = MutableStateFlow(initialState.list)
    private val itemsState = MutableStateFlow(initialState.items)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private var processingJob: Job? = null

    init {
        loadData()
        observeLists()
    }

    private fun observeLists() {
        viewModelScope.launch {
            merge(
                localListsSource.observeUpdates(),
                localListsItemsSource.observeUpdates(),
            )
                .distinctUntilChanged()
                .debounce(250)
                .collect {
                    loadLocalData()
                }
        }
    }

    private fun loadLocalData() {
        viewModelScope.launch {
            try {
                listState.update {
                    getListUseCase.getLocalList(listId)
                }
                itemsState.update {
                    getListItemsUseCase.getLocalItems(listId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            }
        }
    }

    private fun loadData(ignoreErrors: Boolean = false) {
        viewModelScope.launch {
            try {
                listState.update {
                    getListUseCase.getLocalList(listId)
                }

                val localItems = getListItemsUseCase.getLocalItems(listId)

                if (localItems.isNotEmpty()) {
                    itemsState.update { localItems }
                    loadingState.update { DONE }
                } else {
                    loadingState.update { LOADING }
                }

                itemsState.update {
                    getListItemsUseCase.getItems(
                        listId = listId,
                        limit = LISTS_SECTION_LIMIT,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                }
            } finally {
                loadingState.update { DONE }
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

    fun clearNavigation() {
        navigateShow.update { null }
        navigateMovie.update { null }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<ListsPersonalState> = combine(
        listState,
        userState,
        itemsState,
        navigateShow,
        navigateMovie,
        loadingState,
        errorState,
    ) { states ->
        ListsPersonalState(
            list = states[0] as CustomList?,
            user = states[1] as User?,
            items = states[2] as ImmutableList<PersonalListItem>?,
            navigateShow = states[3] as TraktId?,
            navigateMovie = states[4] as TraktId?,
            loading = states[5] as LoadingState,
            error = states[6] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

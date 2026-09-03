package tv.trakt.trakt.core.summary.movies.features.context.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserListsUseCase
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.errors.GlobalErrorsManager
import tv.trakt.trakt.common.helpers.extensions.HTTP_ERROR_TRAKT_VIP_LIMIT
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.lists.CustomListMinimal
import tv.trakt.trakt.core.lists.sections.personal.usecases.manage.AddPersonalListItemUseCase
import tv.trakt.trakt.core.lists.sections.personal.usecases.manage.RemovePersonalListItemUseCase

internal class MovieDetailsListsViewModel(
    private val movie: Movie,
    private val sessionManager: SessionManager,
    private val loadListsUseCase: LoadUserListsUseCase,
    private val addListItemUseCase: AddPersonalListItemUseCase,
    private val removeListItemUseCase: RemovePersonalListItemUseCase,
    private val errorsManager: GlobalErrorsManager,
) : ViewModel() {
    private val initialState = MovieDetailsListsState()

    private val listsState = MutableStateFlow(initialState.lists)
    private val movieListsState = MutableStateFlow(initialState.movieLists)
    private val togglingState = MutableStateFlow(initialState.toggling)
    private val userState = MutableStateFlow(initialState.user)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            sessionManager.getProfile()
                .also { user ->
                    userState.update { user }
                } ?: return@launch

            try {
                loadingState.update { LoadingState.Loading }

                listsState.update {
                    loadListsUseCase.loadLocalLists()
                        .map { it.value }
                        .toImmutableList()
                }

                movieListsState.update {
                    loadListsUseCase.loadMovieLists(movie.ids.trakt)
                }

                listsState.update { lists ->
                    lists.sortedBy {
                        when {
                            movieListsState.value.contains(it.id) -> 0
                            else -> 1
                        }
                    }.toImmutableList()
                }

                loadingState.update { LoadingState.Done }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                    loadingState.update { LoadingState.Idle }
                }
            }
        }
    }

    fun toggleList(list: CustomListMinimal) {
        if (loadingState.value.isLoading ||
            togglingState.value.contains(list.id) ||
            userState.value == null
        ) {
            return
        }

        val listed = isListed(list.id)
        togglingState.update { (it + list.id).toImmutableSet() }
        setListed(list.id, listed = !listed)

        viewModelScope.launch {
            try {
                when {
                    listed -> removeListItemUseCase.removeMovie(
                        listId = list.id,
                        ownerId = list.ownerId,
                        movieId = movie.ids.trakt,
                    )
                    else -> addListItemUseCase.addMovie(
                        listId = list.id,
                        ownerId = list.ownerId,
                        movie = movie,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    setListed(list.id, listed = listed)
                    when (error.getHttpCode()) {
                        HTTP_ERROR_TRAKT_VIP_LIMIT -> {
                            errorsManager.tryEmit(error)
                        }
                        else -> {
                            errorState.update { error }
                            Timber.recordError(error)
                        }
                    }
                }
            } finally {
                togglingState.update { (it - list.id).toImmutableSet() }
            }
        }
    }

    private fun isListed(listId: TraktId): Boolean {
        return movieListsState.value.contains(listId)
    }

    private fun setListed(
        listId: TraktId,
        listed: Boolean,
    ) {
        movieListsState.update {
            when {
                listed -> (it + listId).toImmutableSet()
                else -> (it - listId).toImmutableSet()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        userState,
        listsState,
        movieListsState,
        togglingState,
        loadingState,
        errorState,
    ) { state ->
        MovieDetailsListsState(
            user = state[0] as User?,
            lists = state[1] as ImmutableList<CustomListMinimal>,
            movieLists = state[2] as ImmutableSet<TraktId>,
            toggling = state[3] as ImmutableSet<TraktId>,
            loading = state[4] as LoadingState,
            error = state[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

package tv.trakt.trakt.core.summary.movies.features.context.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.CustomListMinimal
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.user.usecases.lists.LoadUserListsUseCase

internal class MovieDetailsListsViewModel(
    private val movie: Movie,
    private val sessionManager: SessionManager,
    private val loadListsUseCase: LoadUserListsUseCase,
) : ViewModel() {
    private val initialState = MovieDetailsListsState()

    private val listsState = MutableStateFlow(initialState.lists)
    private val movieListsState = MutableStateFlow(initialState.movieLists)
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

    fun isListed(listId: TraktId): Boolean {
        return movieListsState.value.contains(listId)
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        userState,
        listsState,
        movieListsState,
        loadingState,
        errorState,
    ) { state ->
        MovieDetailsListsState(
            user = state[0] as User?,
            lists = state[1] as ImmutableList<CustomListMinimal>,
            movieLists = state[2] as ImmutableSet<TraktId>,
            loading = state[3] as LoadingState,
            error = state[4] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

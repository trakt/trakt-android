package tv.trakt.trakt.core.summary.movies.features.context.lists

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
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaType.MOVIE
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.user.usecases.lists.LoadUserListsUseCase

internal class MovieDetailsListsViewModel(
    private val movie: Movie,
    private val sessionManager: SessionManager,
    private val loadListsUseCase: LoadUserListsUseCase,
) : ViewModel() {
    private val initialState = MovieDetailsListsState()

    private val listsState = MutableStateFlow(initialState.lists)
    private val userState = MutableStateFlow(initialState.user)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val user = sessionManager.getProfile()
                .also { user ->
                    userState.update { user }
                }

            if (user == null) {
                return@launch
            }

            try {
                viewModelScope.launch {
                    val lists = loadListsUseCase.loadLocalLists()
                        .map { (list, items) ->
                            val containsMovie = items.any {
                                it.id == movie.ids.trakt &&
                                    it.type == MOVIE
                            }
                            list to containsMovie
                        }

                    listsState.update {
                        lists
                            .sortedBy { it.first.name }
                            .toImmutableList()
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            }
        }
    }

    fun isInList(listId: TraktId): Boolean {
        return listsState.value.any { (list, inList) ->
            list.ids.trakt == listId && inList
        }
    }

    val state: StateFlow<MovieDetailsListsState> = combine(
        userState,
        listsState,
        errorState,
    ) { s1, s2, s3 ->
        MovieDetailsListsState(
            user = s1,
            lists = s2,
            error = s3,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

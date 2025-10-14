package tv.trakt.trakt.core.summary.shows

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.isNowOrBefore
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.summary.shows.navigation.ShowDetailsDestination
import tv.trakt.trakt.core.summary.shows.usecases.GetShowDetailsUseCase
import tv.trakt.trakt.core.summary.shows.usecases.GetShowRatingsUseCase
import tv.trakt.trakt.core.summary.shows.usecases.GetShowStudiosUseCase

internal class ShowDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getDetailsUseCase: GetShowDetailsUseCase,
    private val getExternalRatingsUseCase: GetShowRatingsUseCase,
    private val getShowStudiosUseCase: GetShowStudiosUseCase,
//    private val loadProgressUseCase: LoadUserProgressUseCase,
//    private val loadWatchlistUseCase: LoadUserWatchlistUseCase,
//    private val loadListsUseCase: LoadUserListsUseCase,
//    private val updateShowHistoryUseCase: UpdateShowHistoryUseCase,
//    private val updateShowWatchlistUseCase: UpdateShowWatchlistUseCase,
//    private val addListItemUseCase: AddPersonalListItemUseCase,
//    private val removeListItemUseCase: RemovePersonalListItemUseCase,
//    private val userWatchlistLocalSource: UserWatchlistLocalDataSource,
//    private val allActivityLocalSource: AllActivityLocalDataSource,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<ShowDetailsDestination>()
    private val showId = destination.showId.toTraktId()

    private val initialState = ShowDetailsState()

    private val showState = MutableStateFlow(initialState.show)
    private val showRatingsState = MutableStateFlow(initialState.showRatings)
    private val showStudiosState = MutableStateFlow(initialState.showStudios)
    private val showProgressState = MutableStateFlow(initialState.showProgress)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingProgress = MutableStateFlow(initialState.loadingProgress)
    private val loadingLists = MutableStateFlow(initialState.loadingLists)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)
    private val userState = MutableStateFlow(initialState.user)

    init {
        loadUser()
        loadData()
//        loadProgressData()
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                userState.update {
                    sessionManager.getProfile()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error)
                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                var show = getDetailsUseCase.getLocalShow(showId)
                if (show == null) {
                    loadingState.update { LOADING }
                    show = getDetailsUseCase.getShow(
                        showId = showId,
                    )
                }
                showState.update { show }

                loadRatings(show)
                loadStudios()
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

    private fun loadRatings(show: Show?) {
        if (show?.released?.isNowOrBefore() != true) {
            // Don't load ratings for unreleased movies
            return
        }
        viewModelScope.launch {
            try {
                showRatingsState.update {
                    getExternalRatingsUseCase.getExternalRatings(showId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error)
                }
            }
        }
    }

    private fun loadStudios() {
        viewModelScope.launch {
            try {
                showStudiosState.update {
                    getShowStudiosUseCase.getStudios(showId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error)
                }
            }
        }
    }

    fun clearInfo() {
        infoState.update { null }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<ShowDetailsState> = combine(
        showState,
        showRatingsState,
        showStudiosState,
        showProgressState,
        loadingState,
        loadingProgress,
        loadingLists,
        infoState,
        errorState,
        userState,
    ) { state ->
        ShowDetailsState(
            show = state[0] as Show?,
            showRatings = state[1] as ExternalRating?,
            showStudios = state[2] as ImmutableList<String>?,
            showProgress = state[3] as ShowDetailsState.ProgressState?,
            loading = state[4] as LoadingState,
            loadingProgress = state[5] as LoadingState,
            loadingLists = state[6] as LoadingState,
            info = state[7] as StringResource?,
            error = state[8] as Exception?,
            user = state[9] as User?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

package tv.trakt.trakt.core.summary.people

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.people.usecases.GetPersonCreditsUseCase
import tv.trakt.trakt.core.people.usecases.GetPersonUseCase
import tv.trakt.trakt.core.summary.people.model.PersonCreditItem
import tv.trakt.trakt.core.summary.people.navigation.PersonDestination
import tv.trakt.trakt.core.user.CollectionStateProvider
import tv.trakt.trakt.core.user.UserCollectionState

internal class PersonDetailsViewModel(
    analytics: Analytics,
    savedStateHandle: SavedStateHandle,
    private val sessionManager: SessionManager,
    private val getPersonUseCase: GetPersonUseCase,
    private val getPersonCreditsUseCase: GetPersonCreditsUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val collectionStateProvider: CollectionStateProvider,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<PersonDestination>()
    private val initialState = PersonDetailsState()

    private val loadingDetailsState = MutableStateFlow(initialState.loadingDetails)
    private val loadingCreditsState = MutableStateFlow(initialState.loadingCredits)

    private val userState = MutableStateFlow(initialState.user)
    private val personDetailsState = MutableStateFlow(initialState.personDetails)
    private val personBackdropState = MutableStateFlow(destination.backdropUrl)
    private val personShowCreditsState = MutableStateFlow(initialState.personShowCredits)
    private val personMovieCreditsState = MutableStateFlow(initialState.personMovieCredits)

    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadUser()
        loadData()
        observeCollection()

        analytics.logScreenView(
            screenName = "person_details",
        )
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

    private fun loadData() {
        viewModelScope.launch {
            loadingDetailsState.update { LoadingState.LOADING }

            val person = getPersonUseCase.getPerson(
                personId = destination.personId.toTraktId(),
            )

            person?.let { person ->
                personDetailsState.update { person }

                loadPersonDetails(person.ids.trakt)
                loadingDetailsState.update { LoadingState.DONE }

                loadPersonCredits(person.ids.trakt)
            }
        }
    }

    private suspend fun loadPersonDetails(personId: TraktId) {
        if (personDetailsState.value?.biography != null) {
            // Skip if biography is already available.
            return
        }
        try {
            personDetailsState.update {
                getPersonUseCase.getPersonDetails(personId)
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                errorState.update { error }
                Timber.recordError(error)
            }
        }
    }

    private suspend fun loadPersonCredits(personId: TraktId) {
        try {
            loadingCreditsState.update { LoadingState.LOADING }

            coroutineScope {
                val showCreditsAsync = async { getPersonCreditsUseCase.getShowCredits(personId) }
                val movieCreditsAsync = async { getPersonCreditsUseCase.getMovieCredits(personId) }

                val showCredits = showCreditsAsync.await()
                val movieCredits = movieCreditsAsync.await()

                personShowCreditsState.update { showCredits }
                personMovieCreditsState.update { movieCredits }
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                errorState.update { error }
                Timber.recordError(error)
            }
        } finally {
            loadingCreditsState.update { LoadingState.DONE }
        }
    }

    fun navigateToShow(show: Show) {
        if (navigateShow.value != null) {
            return
        }
        viewModelScope.launch {
            showLocalDataSource.upsertShows(listOf(show))
            navigateShow.update { show.ids.trakt }
        }
    }

    fun navigateToMovie(movie: Movie) {
        if (navigateMovie.value != null) {
            return
        }
        viewModelScope.launch {
            movieLocalDataSource.upsertMovies(listOf(movie))
            navigateMovie.update { movie.ids.trakt }
        }
    }

    fun isCurrentMediaId(targetId: TraktId): Boolean {
        return destination.sourceMediaId == targetId.value
    }

    fun clearNavigation() {
        navigateShow.update { null }
        navigateMovie.update { null }
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        userState,
        loadingDetailsState,
        loadingCreditsState,
        personDetailsState,
        personBackdropState,
        personShowCreditsState,
        personMovieCreditsState,
        collectionStateProvider.stateFlow,
        navigateShow,
        navigateMovie,
        errorState,
    ) { state ->
        PersonDetailsState(
            user = state[0] as User?,
            loadingDetails = state[1] as LoadingState,
            loadingCredits = state[2] as LoadingState,
            personDetails = state[3] as Person?,
            personBackdropUrl = state[4] as String?,
            personShowCredits = state[5] as ImmutableMap<String, ImmutableList<PersonCreditItem.ShowItem>>?,
            personMovieCredits = state[6] as ImmutableMap<String, ImmutableList<PersonCreditItem.MovieItem>>?,
            collection = state[7] as UserCollectionState,
            navigateShow = state[8] as TraktId?,
            navigateMovie = state[9] as TraktId?,
            error = state[10] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

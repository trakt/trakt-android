package tv.trakt.trakt.core.summary.people

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.media.movies.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.media.shows.local.ShowLocalDataSource
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.SlugId
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.toSlugId
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.people.usecases.GetPersonCreditsUseCase
import tv.trakt.trakt.core.people.usecases.GetPersonUseCase
import tv.trakt.trakt.core.summary.movies.usecases.GetMovieDetailsUseCase
import tv.trakt.trakt.core.summary.people.model.PersonCreditItem
import tv.trakt.trakt.core.summary.people.navigation.PersonDestination
import tv.trakt.trakt.core.summary.shows.usecases.GetShowDetailsUseCase
import java.net.HttpURLConnection.HTTP_NOT_FOUND

internal class PersonDetailsViewModel(
    analytics: Analytics,
    savedStateHandle: SavedStateHandle,
    private val sessionManager: SessionManager,
    private val getPersonUseCase: GetPersonUseCase,
    private val getPersonCreditsUseCase: GetPersonCreditsUseCase,
    private val getShowDetailsUseCase: GetShowDetailsUseCase,
    private val getMovieDetailsUseCase: GetMovieDetailsUseCase,
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
    private val preferredCreditsFilterState = MutableStateFlow(destination.creditsFilter)
    private val personShowCreditsState = MutableStateFlow(initialState.personShowCredits)
    private val personMovieCreditsState = MutableStateFlow(initialState.personMovieCredits)

    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val errorState = MutableStateFlow(initialState.error)

    private var mediaLinkJob: Job? = null

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
            loadingDetailsState.update { LoadingState.Loading }

            val person = try {
                loadPerson()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
                loadingDetailsState.update { LoadingState.Done }
                return@launch
            }

            personDetailsState.update { person }

            loadPersonDetails(person.ids.trakt)
            loadingDetailsState.update { LoadingState.Done }

            loadPersonCredits(person.ids.trakt)
        }
    }

    private suspend fun loadPerson(): Person {
        destination.personSlug?.let {
            return getPersonUseCase.getPersonDetails(it.toSlugId())
        }

        val personId = checkNotNull(destination.personId) {
            "PersonDestination requires a personId or personSlug"
        }
        return getPersonUseCase.getPerson(personId.toTraktId())
    }

    private suspend fun loadPersonDetails(personId: TraktId) {
        if (personDetailsState.value?.biography != null) {
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
        loadingCreditsState.update { LoadingState.Loading }

        supervisorScope {
            val showCreditsAsync = async { getPersonCreditsUseCase.getShowCredits(personId) }
            val movieCreditsAsync = async { getPersonCreditsUseCase.getMovieCredits(personId) }

            try {
                personShowCreditsState.update { showCreditsAsync.await() }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            }

            try {
                personMovieCreditsState.update { movieCreditsAsync.await() }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            }
        }

        loadingCreditsState.update { LoadingState.Done }
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

    fun navigateToShow(showSlug: SlugId) {
        openMediaLink {
            val show = personShowCreditsState.value
                ?.values
                ?.firstNotNullOfOrNull { items -> items.firstOrNull { it.show.ids.slug == showSlug }?.show }
                ?: getShowDetailsUseCase.getShow(showSlug)
            navigateToShow(show)
        }
    }

    fun navigateToMovie(movieSlug: SlugId) {
        openMediaLink {
            val movie = personMovieCreditsState.value
                ?.values
                ?.firstNotNullOfOrNull { items -> items.firstOrNull { it.movie.ids.slug == movieSlug }?.movie }
                ?: getMovieDetailsUseCase.getMovie(movieSlug)
            navigateToMovie(movie)
        }
    }

    // Biography links carry only a slug, while details destinations expect Trakt IDs.
    private fun openMediaLink(resolve: suspend () -> Unit) {
        mediaLinkJob?.cancel()
        mediaLinkJob = viewModelScope.launch {
            try {
                resolve()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (error.getHttpCode() != HTTP_NOT_FOUND) {
                        Timber.recordError(error)
                    }
                }
            }
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
        preferredCreditsFilterState,
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
            preferredCreditsFilter = state[11] as String?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

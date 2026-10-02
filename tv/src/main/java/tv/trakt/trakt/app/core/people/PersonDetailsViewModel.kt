package tv.trakt.trakt.app.core.people

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.app.core.people.helpers.buildHistoryCredits
import tv.trakt.trakt.app.core.people.navigation.PersonDestination
import tv.trakt.trakt.app.core.people.usecases.GetPersonCreditsUseCase
import tv.trakt.trakt.app.core.people.usecases.GetPersonUseCase
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId

@Suppress("UNCHECKED_CAST")
internal class PersonDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getPersonUseCase: GetPersonUseCase,
    private val getPersonCreditsUseCase: GetPersonCreditsUseCase,
    private val collectionStateProvider: CollectionStateProvider,
) : ViewModel() {
    private val initialState = PersonDetailsState()

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val personDetailsState = MutableStateFlow(initialState.personDetails)
    private val personBackdropState = MutableStateFlow(initialState.personBackdropUrl)
    private val personShowCreditsState = MutableStateFlow(initialState.personShowCredits)
    private val personMovieCreditsState = MutableStateFlow(initialState.personMovieCredits)
    private val errorState = MutableStateFlow(initialState.error)

    private val destination = savedStateHandle.toRoute<PersonDestination>()

    init {
        personBackdropState.value = destination.backdropUrl
        loadData(TraktId(destination.personId))
        observeCollection()
    }

    private fun observeCollection() {
        collectionStateProvider
            .launchIn(viewModelScope)
    }

    private fun loadData(personId: TraktId) {
        viewModelScope.launch {
            personDetailsState.update {
                getPersonUseCase.getPerson(personId)
            }
            loadPersonDetails(personId)
            loadPersonCredits(personId)
        }
    }

    private fun loadPersonDetails(personId: TraktId) {
        if (personDetailsState.value?.biography != null) {
            // Skip if biography is already available.
            return
        }
        viewModelScope.launch {
            try {
                personDetailsState.update {
                    getPersonUseCase.getPersonDetails(personId)
                }
            } catch (e: Exception) {
                e.rethrowCancellation()
            }
        }
    }

    private fun loadPersonCredits(personId: TraktId) {
        viewModelScope.launch {
            try {
                coroutineScope {
                    val showCreditsAsync = async { getPersonCreditsUseCase.getShowCredits(personId) }
                    val movieCreditsAsync = async { getPersonCreditsUseCase.getMovieCredits(personId) }

                    val showCredits = showCreditsAsync.await()
                    val movieCredits = movieCreditsAsync.await()

                    personShowCreditsState.value = showCredits
                    personMovieCreditsState.value = movieCredits
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e("Error loading person credits: ${error.message}")
                }
            }
        }
    }

    fun validateSourceId(targetId: TraktId): Boolean {
        return destination.sourceId != targetId.value
    }

    val state = combine(
        loadingState,
        personDetailsState,
        personBackdropState,
        personShowCreditsState,
        personMovieCreditsState,
        errorState,
        collectionStateProvider.stateFlow,
    ) { state ->
        val showCredits = state[3] as ImmutableList<Show>?
        val movieCredits = state[4] as ImmutableList<Movie>?

        PersonDetailsState(
            isLoading = state[0] as Boolean,
            personDetails = state[1] as Person?,
            personBackdropUrl = state[2] as String?,
            personShowCredits = showCredits,
            personMovieCredits = movieCredits,
            personHistoryCredits = buildHistoryCredits(
                shows = showCredits,
                movies = movieCredits,
                collection = state[6] as UserCollectionState,
            ),
            error = state[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

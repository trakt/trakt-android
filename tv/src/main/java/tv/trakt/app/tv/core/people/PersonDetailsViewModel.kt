package tv.trakt.app.tv.core.people

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.app.tv.common.model.Person
import tv.trakt.app.tv.common.model.TraktId
import tv.trakt.app.tv.core.movies.model.Movie
import tv.trakt.app.tv.core.people.navigation.PersonDestination
import tv.trakt.app.tv.core.people.usecases.GetPersonCreditsUseCase
import tv.trakt.app.tv.core.people.usecases.GetPersonUseCase
import tv.trakt.app.tv.core.shows.model.Show
import tv.trakt.app.tv.helpers.extensions.rethrowCancellation

internal class PersonDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getPersonUseCase: GetPersonUseCase,
    private val getPersonCreditsUseCase: GetPersonCreditsUseCase,
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
    }

    private fun loadData(personId: TraktId) {
        viewModelScope.launch {
            val person = getPersonUseCase.getPerson(personId)
            person?.let {
                personDetailsState.value = it
                loadPersonDetails(personId)
                loadPersonCredits(personId)
            }
        }
    }

    private fun loadPersonDetails(personId: TraktId) {
        if (personDetailsState.value?.biography != null) {
            // Skip if biography is already available.
            return
        }
        viewModelScope.launch {
            try {
                val personDetails = getPersonUseCase.getPersonDetails(personId)
                personDetailsState.value = personDetails
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
                    Log.e("PersonDetailsViewModel", "Error loading person credits: ${error.message}")
                }
            }
        }
    }

    fun validateSourceId(targetId: TraktId): Boolean {
        return destination.sourceId != targetId.value
    }

    val state: StateFlow<PersonDetailsState> = combine(
        loadingState,
        personDetailsState,
        personBackdropState,
        personShowCreditsState,
        personMovieCreditsState,
        errorState,
    ) { states ->
        @Suppress("UNCHECKED_CAST")
        PersonDetailsState(
            isLoading = states[0] as Boolean,
            personDetails = states[1] as Person?,
            personBackdropUrl = states[2] as String?,
            personShowCredits = states[3] as ImmutableList<Show>?,
            personMovieCredits = states[4] as ImmutableList<Movie>?,
            error = states[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

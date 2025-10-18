package tv.trakt.trakt.core.summary.people

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
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.people.usecases.GetPersonUseCase
import tv.trakt.trakt.core.summary.people.navigation.PersonDestination

internal class PersonDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getPersonUseCase: GetPersonUseCase,
//    private val getPersonCreditsUseCase: GetPersonCreditsUseCase,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<PersonDestination>()
    private val initialState = PersonDetailsState()

    private val loadingState = MutableStateFlow(initialState.loading)
    private val personDetailsState = MutableStateFlow(initialState.personDetails)
    private val personBackdropState = MutableStateFlow(destination.backdropUrl)
    private val personShowCreditsState = MutableStateFlow(initialState.personShowCredits)
    private val personMovieCreditsState = MutableStateFlow(initialState.personMovieCredits)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val person = getPersonUseCase.getPerson(
                personId = destination.personId.toTraktId(),
            )
            person?.let { person ->
                personDetailsState.update { person }
                loadPersonDetails(person.ids.trakt)
//                loadPersonCredits(personId)
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
                personDetailsState.update {
                    getPersonUseCase.getPersonDetails(personId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.w("Error loading person details: ${error.message}")
                }
            }
        }
    }

//    private fun loadPersonDetails(personId: TraktId) {
// //        if (personDetailsState.value?.biography != null) {
// //            // Skip if biography is already available.
// //            return
// //        }
//        viewModelScope.launch {
//            try {
//                personDetailsState.update {
//                    getPersonUseCase.getPerson(personId)
//                }
//            } catch (e: Exception) {
//                e.rethrowCancellation()
//            }
//        }
//    }

//    private fun loadPersonCredits(personId: TraktId) {
//        viewModelScope.launch {
//            try {
//                coroutineScope {
//                    val showCreditsAsync = async { getPersonCreditsUseCase.getShowCredits(personId) }
//                    val movieCreditsAsync = async { getPersonCreditsUseCase.getMovieCredits(personId) }
//
//                    val showCredits = showCreditsAsync.await()
//                    val movieCredits = movieCreditsAsync.await()
//
//                    personShowCreditsState.value = showCredits
//                    personMovieCreditsState.value = movieCredits
//                }
//            } catch (error: Exception) {
//                error.rethrowCancellation {
//                    errorState.update { error }
//                    Timber.e("Error loading person credits: ${error.message}")
//                }
//            }
//        }
//    }

    fun checkSourceMediaId(targetId: TraktId): Boolean {
        return destination.sourceMediaId != targetId.value
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<PersonDetailsState> = combine(
        loadingState,
        personDetailsState,
        personBackdropState,
        personShowCreditsState,
        personMovieCreditsState,
        errorState,
    ) { state ->
        PersonDetailsState(
            loading = state[0] as LoadingState,
            personDetails = state[1] as Person?,
            personBackdropUrl = state[2] as String?,
            personShowCredits = state[3] as ImmutableList<Show>?,
            personMovieCredits = state[4] as ImmutableList<Movie>?,
            error = state[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

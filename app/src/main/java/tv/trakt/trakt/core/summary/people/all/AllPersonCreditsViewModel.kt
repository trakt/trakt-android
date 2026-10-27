package tv.trakt.trakt.core.summary.people.all

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import timber.log.Timber
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.people.usecases.GetPersonCreditsUseCase
import tv.trakt.trakt.core.people.usecases.GetPersonUseCase
import tv.trakt.trakt.core.summary.people.helpers.historyPersonCredits
import tv.trakt.trakt.core.summary.people.helpers.mergePersonCredits
import tv.trakt.trakt.core.summary.people.helpers.resolveSelectedCreditsFilter
import tv.trakt.trakt.core.summary.people.model.PersonCreditItem

private typealias ShowCredits = ImmutableMap<String, ImmutableList<PersonCreditItem.ShowItem>>
private typealias MovieCredits = ImmutableMap<String, ImmutableList<PersonCreditItem.MovieItem>>

@Suppress("UNCHECKED_CAST")
internal class AllPersonCreditsViewModel(
    savedStateHandle: SavedStateHandle,
    private val analytics: Analytics,
    private val getPersonUseCase: GetPersonUseCase,
    private val getPersonCreditsUseCase: GetPersonCreditsUseCase,
    private val showLocalSource: ShowLocalSource,
    private val movieLocalSource: MovieLocalSource,
    private val collectionStateProvider: CollectionStateProvider,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<AllPersonCreditsDestination>()
    private val personId = destination.personId.toTraktId()
    private val initialState = AllPersonCreditsState(
        section = destination.section,
        mode = destination.section.initialMode,
    )

    private val modeState = MutableStateFlow(initialState.mode)
    private val positionState = MutableStateFlow(destination.position)
    private val personState = MutableStateFlow(initialState.person)
    private val showCreditsState = MutableStateFlow<ShowCredits?>(null)
    private val movieCreditsState = MutableStateFlow<MovieCredits?>(null)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
        collectionStateProvider.launchIn(viewModelScope)

        analytics.logScreenView(
            screenName = destination.section.screenName,
        )
        analytics.logDrilldown(
            source = destination.section.drilldownSource,
            type = null,
        )
    }

    private fun loadData() {
        viewModelScope.launch {
            loadingState.update { LoadingState.Loading }

            supervisorScope {
                val personAsync = async { getPersonUseCase.getPerson(personId) }
                val showCreditsAsync = async { getPersonCreditsUseCase.getShowCredits(personId) }
                val movieCreditsAsync = async { getPersonCreditsUseCase.getMovieCredits(personId) }

                runLoad { personState.update { personAsync.await() } }
                runLoad { showCreditsState.update { showCreditsAsync.await() } }
                runLoad { movieCreditsState.update { movieCreditsAsync.await() } }
            }

            loadingState.update { LoadingState.Done }
        }
    }

    private suspend fun runLoad(block: suspend () -> Unit) {
        try {
            block()
        } catch (error: Exception) {
            error.rethrowCancellation {
                errorState.update { error }
                Timber.recordError(error)
            }
        }
    }

    fun setMode(mode: MediaMode) {
        modeState.update { mode }
    }

    fun setPosition(position: String) {
        positionState.update { position }
    }

    fun navigateToShow(show: Show) {
        if (navigateShow.value != null) {
            return
        }
        viewModelScope.launch {
            showLocalSource.upsertShows(listOf(show))
            navigateShow.update { show.ids.trakt }
        }
    }

    fun navigateToMovie(movie: Movie) {
        if (navigateMovie.value != null) {
            return
        }
        viewModelScope.launch {
            movieLocalSource.upsertMovies(listOf(movie))
            navigateMovie.update { movie.ids.trakt }
        }
    }

    fun clearNavigation() {
        navigateShow.update { null }
        navigateMovie.update { null }
    }

    private fun buildState(
        mode: MediaMode,
        requestedPosition: String?,
        person: Person?,
        showCredits: ShowCredits?,
        movieCredits: MovieCredits?,
        collection: UserCollectionState,
    ): AllPersonCreditsState {
        if (destination.section.isHistory) {
            return initialState.copy(
                mode = mode,
                person = person,
                items = historyPersonCredits(showCredits, movieCredits, collection, mode),
                collection = collection,
            )
        }

        val credits = mergePersonCredits(showCredits, movieCredits, mode)
        val selectedPosition = resolveSelectedCreditsFilter(
            listItems = credits,
            knownForDepartment = person?.knownForDepartment,
            preferredDepartment = requestedPosition,
        )

        return initialState.copy(
            mode = mode,
            person = person,
            positions = credits.keys.toImmutableList(),
            selectedPosition = selectedPosition,
            items = selectedPosition?.let { credits[it] } ?: initialState.items,
            collection = collection,
        )
    }

    val state = combine(
        modeState,
        positionState,
        personState,
        showCreditsState,
        movieCreditsState,
        collectionStateProvider.stateFlow,
        loadingState,
        navigateShow,
        navigateMovie,
        errorState,
    ) { state ->
        buildState(
            mode = state[0] as MediaMode,
            requestedPosition = state[1] as String?,
            person = state[2] as Person?,
            showCredits = state[3] as ShowCredits?,
            movieCredits = state[4] as MovieCredits?,
            collection = state[5] as UserCollectionState,
        ).copy(
            loading = state[6] as LoadingState,
            navigateShow = state[7] as TraktId?,
            navigateMovie = state[8] as TraktId?,
            error = state[9] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

package tv.trakt.trakt.core.trivia

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.common.model.trivia.TriviaFact
import tv.trakt.trakt.core.summary.movies.features.trivia.usecases.GetMovieTriviaUseCase
import tv.trakt.trakt.core.summary.shows.features.trivia.usecases.GetShowTriviaUseCase
import tv.trakt.trakt.core.trivia.model.TriviaFilter
import tv.trakt.trakt.core.trivia.model.TriviaFilter.NoSpoilers
import tv.trakt.trakt.core.trivia.model.TriviaFilter.Spoilers
import tv.trakt.trakt.core.trivia.navigation.TriviaDestination

internal class TriviaViewModel(
    savedStateHandle: SavedStateHandle,
    analytics: Analytics,
    private val getMovieTriviaUseCase: GetMovieTriviaUseCase,
    private val getShowTriviaUseCase: GetShowTriviaUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<TriviaDestination>()

    private val initialState = TriviaState(
        backgroundUrl = destination.mediaImage,
        mediaTitle = destination.mediaTitle,
    )

    private val userState = MutableStateFlow(initialState.user)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialState.filter)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
        observeUser()

        analytics.trivia.logScreenView(
            source = destination.navSource,
        )
    }

    @OptIn(FlowPreview::class)
    private fun observeUser() {
        sessionManager.observeProfile()
            .distinctUntilChanged()
            .debounce(200)
            .filter {
                userState.value?.isAnyVip != it?.isAnyVip
            }
            .onEach { user ->
                userState.update { user }
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private var loadDataJob: Job? = null

    private fun loadData() {
        loadDataJob?.cancel()
        loadDataJob = viewModelScope.launch {
            var loadingJob: Job? = null
            try {
                loadingJob = launch {
                    delay(300)
                    loadingState.update { LoadingState.Loading }
                }

                loadUser()
                itemsState.update {
                    val mediaId = destination.mediaId.toTraktId()
                    when (destination.mediaType) {
                        MediaType.MOVIE -> getMovieTriviaUseCase.getTrivia(mediaId)
                        MediaType.SHOW -> getShowTriviaUseCase.getTrivia(mediaId)
                        else -> return@launch
                    }.facts
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
                loadingJob?.cancel()
            }
        }
    }

    private suspend fun loadUser() {
        try {
            userState.update { sessionManager.getProfile() }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }

    fun setFilter(filter: TriviaFilter) {
        filterState.update { current ->
            if (current == filter) null else filter
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        userState,
        loadingState,
        itemsState,
        filterState,
        errorState,
    ) { state ->
        val items = state[2] as ImmutableList<TriviaFact>?
        val filter = state[3] as TriviaFilter?

        TriviaState(
            user = state[0] as User?,
            loading = state[1] as LoadingState,
            items = items,
            filteredItems = when (filter) {
                null -> items
                NoSpoilers -> items?.filter { !it.spoiler }?.toImmutableList()
                Spoilers -> items?.filter { it.spoiler }?.toImmutableList()
            },
            filter = filter,
            backgroundUrl = initialState.backgroundUrl,
            mediaTitle = initialState.mediaTitle,
            error = state[4] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

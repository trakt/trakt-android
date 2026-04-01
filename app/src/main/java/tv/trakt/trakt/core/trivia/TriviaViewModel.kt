package tv.trakt.trakt.core.trivia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
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
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.summary.movies.features.trivia.usecases.GetMovieTriviaUseCase
import tv.trakt.trakt.core.summary.shows.features.trivia.usecases.GetShowTriviaUseCase
import tv.trakt.trakt.core.trivia.model.TriviaFilter
import tv.trakt.trakt.core.trivia.model.TriviaFilter.NoSpoilers
import tv.trakt.trakt.core.trivia.model.TriviaFilter.Spoilers

internal class TriviaViewModel(
    backgroundUrl: String?,
    private val mediaId: TraktId,
    private val mediaType: MediaType,
    private val getMovieTriviaUseCase: GetMovieTriviaUseCase,
    private val getShowTriviaUseCase: GetShowTriviaUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = TriviaState(backgroundUrl = backgroundUrl)

    private val userState = MutableStateFlow(initialState.user)
    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialState.filter)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
        observeUser()
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

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadUser()
                val trivia = when (mediaType) {
                    MediaType.MOVIE -> getMovieTriviaUseCase.getTrivia(mediaId)
                    MediaType.SHOW -> getShowTriviaUseCase.getTrivia(mediaId)
                    else -> return@launch
                }
                itemsState.update { trivia.facts }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
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

    val state = combine(
        userState,
        itemsState,
        filterState,
        errorState,
    ) { user, items, filter, error ->
        val filteredItems = when (filter) {
            null -> items
            NoSpoilers -> items?.filter { !it.spoiler }?.toImmutableList()
            Spoilers -> items?.filter { it.spoiler }?.toImmutableList()
        }
        TriviaState(
            user = user,
            items = items,
            filteredItems = filteredItems,
            filter = filter,
            backgroundUrl = initialState.backgroundUrl,
            error = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

package tv.trakt.trakt.core.summary.shows.features.trivia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
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
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.trivia.TriviaFact
import tv.trakt.trakt.core.summary.shows.features.trivia.usecases.GetShowTriviaUseCase
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

private val collapsingKey = CollapsingKey.SHOW_TRIVIA

internal class ShowTriviaViewModel(
    private val show: Show,
    private val getTriviaUseCase: GetShowTriviaUseCase,
    private val collapsingManager: CollapsingManager,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val initialState = ShowTriviaState()

    private val userState = MutableStateFlow(initialState.user)
    private val summaryState = MutableStateFlow(initialState.summary)
    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val collapseState = MutableStateFlow(collapsingManager.isCollapsed(collapsingKey))
    private val errorState = MutableStateFlow(initialState.error)

    private var collapseJob: kotlinx.coroutines.Job? = null

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
                // Only update if VIP status has changed, as trivia may be VIP locked.
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
                loadingState.update { Loading }

                loadUser()
                val (summary, items) = getTriviaUseCase.getTrivia(show.ids.trakt)

                summaryState.update { summary }
                itemsState.update { items }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    // Ignore 404 errors, as they simply mean no trivia is available.
                    if (error.getHttpCode() != 404) {
                        errorState.update { error }
                        Timber.recordError(error)
                    }
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    private suspend fun loadUser() {
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

    fun setCollapsed(collapsed: Boolean) {
        collapseState.update { collapsed }

        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            when {
                collapsed -> collapsingManager.collapse(collapsingKey)
                else -> collapsingManager.expand(collapsingKey)
            }
        }
    }

    val state = combine(
        userState,
        itemsState,
        summaryState,
        loadingState,
        collapseState,
        errorState,
    ) { state ->
        @Suppress("UNCHECKED_CAST")
        ShowTriviaState(
            user = state[0] as User?,
            items = state[1] as ImmutableList<TriviaFact>?,
            summary = state[2] as ImmutableList<String>?,
            loading = state[3] as LoadingState,
            collapsed = state[4] as Boolean,
            error = state[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

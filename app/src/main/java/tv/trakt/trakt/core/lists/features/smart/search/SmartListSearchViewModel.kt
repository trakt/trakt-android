package tv.trakt.trakt.core.lists.features.smart.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@Suppress("UNCHECKED_CAST")
internal class SmartListSearchViewModel(
    private val kind: SmartListSearchKind,
    private val searchUseCase: SearchSmartListFilterOptionsUseCase,
) : ViewModel() {
    private val initialState = SmartListSearchState()

    private val queryState = MutableStateFlow(initialState.query)
    private val resultsState = MutableStateFlow(initialState.results)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        observeQuery()
    }

    private fun observeQuery() {
        viewModelScope.launch {
            queryState
                .map { it.trim() }
                .debounce(300.milliseconds)
                .distinctUntilChanged()
                .collectLatest(::search)
        }
    }

    private suspend fun search(query: String) {
        if (query.length < MIN_SEARCH_QUERY_LENGTH) {
            resultsState.update { persistentListOf() }
            loadingState.update { Idle }
            return
        }

        loadingState.update { Loading }
        errorState.update { null }

        try {
            resultsState.update { searchUseCase.search(kind = kind, query = query) }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
                errorState.update { error }
            }
        } finally {
            loadingState.update { Idle }
        }
    }

    fun setQuery(query: String) {
        queryState.update { query }
    }

    val state = combine(
        queryState,
        resultsState,
        loadingState,
        errorState,
    ) { state ->
        SmartListSearchState(
            query = state[0] as String,
            results = state[1] as ImmutableList<SmartListSearchOption>,
            loading = state[2] as LoadingState,
            error = state[3] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

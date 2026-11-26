package tv.trakt.trakt.core.mixtape

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.mixtape.Mixtape.Fit
import tv.trakt.trakt.common.model.mixtape.MixtapeItem
import tv.trakt.trakt.common.model.mixtape.MixtapeVectors
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.mixtape.model.MixtapePicker
import tv.trakt.trakt.core.mixtape.usecases.GetMixtapeHistoryUseCase
import tv.trakt.trakt.core.mixtape.usecases.GetMixtapeUseCase
import tv.trakt.trakt.core.mixtape.usecases.GetMixtapeVectorsUseCase
import tv.trakt.trakt.core.mixtape.usecases.MixtapeHintUseCase
import tv.trakt.trakt.core.mixtape.usecases.ScoreMixtapeFitsUseCase
import kotlin.time.Duration.Companion.milliseconds

private val SEARCH_DEBOUNCE = 300.milliseconds

@OptIn(FlowPreview::class)
@Suppress("UNCHECKED_CAST")
internal class MixtapeViewModel(
    filterManager: GlobalFilterManager,
    analytics: Analytics,
    private val mixtapeHintUseCase: MixtapeHintUseCase,
    private val getHistoryUseCase: GetMixtapeHistoryUseCase,
    private val getVectorsUseCase: GetMixtapeVectorsUseCase,
    private val getMixtapeUseCase: GetMixtapeUseCase,
    private val scoreFitsUseCase: ScoreMixtapeFitsUseCase,
) : ViewModel() {
    private val initialState = MixtapeState(
        type = filterManager.getFilter().mode.toMixtapeType(),
    )

    private val typeState = MutableStateFlow(initialState.type)
    private val queryState = MutableStateFlow("")
    private val picksState = MutableStateFlow(initialState.picks)
    private val historyState = MutableStateFlow<ImmutableList<MixtapeItem>>(persistentListOf())
    private val historyLoadingState = MutableStateFlow(true)
    private val searchResultsState = MutableStateFlow<ImmutableList<MixtapeItem>>(persistentListOf())
    private val searchLoadingState = MutableStateFlow(false)
    private val deepFitsState = MutableStateFlow<ImmutableList<Fit>>(persistentListOf())
    private val deepLoadingState = MutableStateFlow(false)
    private val knownState = MutableStateFlow<PersistentMap<TraktId, MixtapeItem>>(persistentMapOf())
    private val vectorsState = MutableStateFlow(MixtapeVectors())

    private val requestedVectorIds = mutableSetOf<TraktId>()
    private var settledPicker: MixtapePicker? = null

    init {
        dismissHint()
        observeHistory()
        observeSearch()
        observeDeepFits()

        analytics.logScreenView(
            screenName = "mixtape",
        )
    }

    private fun dismissHint() {
        viewModelScope.launch {
            mixtapeHintUseCase.dismissHint()
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            typeState.collectLatest { type ->
                try {
                    historyLoadingState.update { true }
                    val history = getHistoryUseCase.getHistory(type)
                    historyState.update { history }
                    addKnown(history)
                    loadVectors(type, history.map { it.id })
                } catch (error: Exception) {
                    error.rethrowCancellation {
                        Timber.recordError(error)
                    }
                } finally {
                    historyLoadingState.update { false }
                }
            }
        }
    }

    private fun observeSearch() {
        viewModelScope.launch {
            queryState
                .map { it.trim() }
                .distinctUntilChanged()
                .debounce(SEARCH_DEBOUNCE)
                .collectLatest { query ->
                    if (query.isEmpty()) {
                        searchResultsState.update { persistentListOf() }
                        searchLoadingState.update { false }
                        return@collectLatest
                    }

                    try {
                        searchLoadingState.update { true }
                        val type = typeState.value
                        val results = getHistoryUseCase.getHistory(type, query)
                        searchResultsState.update { results }
                        addKnown(results)
                        loadVectors(type, results.map { it.id })
                    } catch (error: Exception) {
                        error.rethrowCancellation {
                            Timber.recordError(error)
                        }
                    } finally {
                        searchLoadingState.update { false }
                    }
                }
        }
    }

    private fun observeDeepFits() {
        viewModelScope.launch {
            combine(typeState, picksState) { type, picks -> type to picks.map { it.id } }
                .distinctUntilChanged()
                .collectLatest { (type, picks) ->
                    deepFitsState.update { persistentListOf() }
                    if (picks.isEmpty()) {
                        deepLoadingState.update { false }
                        return@collectLatest
                    }

                    try {
                        deepLoadingState.update { true }
                        loadVectors(type, picks)
                        val mixtape = getMixtapeUseCase.getMixtape(
                            type = type,
                            picks = picks,
                            suggestions = false,
                        )
                        deepFitsState.update { mixtape.fits }
                        addKnown(mixtape.fits.mapNotNull { it.item })
                    } catch (error: Exception) {
                        error.rethrowCancellation {
                            Timber.recordError(error)
                        }
                    } finally {
                        deepLoadingState.update { false }
                    }
                }
        }
    }

    private fun addKnown(items: List<MixtapeItem>) {
        knownState.update { known ->
            known.putAll(items.filter { it.type == typeState.value }.associateBy { it.id })
        }
    }

    private fun loadVectors(
        type: MediaType,
        ids: List<TraktId>,
    ) {
        val missing = ids
            .filterNot { it in vectorsState.value.vectors || it in requestedVectorIds }
            .distinct()
        if (missing.isEmpty()) return

        requestedVectorIds += missing
        viewModelScope.launch {
            try {
                val fetched = getVectorsUseCase.getVectors(type, missing)
                if (type != typeState.value) return@launch

                vectorsState.update { current ->
                    MixtapeVectors(
                        cutoff = if (current.vectors.isEmpty()) fetched.cutoff else current.cutoff,
                        vectors = (current.vectors + fetched.vectors).toImmutableMap(),
                    )
                }
            } catch (error: Exception) {
                requestedVectorIds -= missing.toSet()
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun setType(type: MediaType) {
        if (type == typeState.value) return

        picksState.update { persistentListOf() }
        historyState.update { persistentListOf() }
        searchResultsState.update { persistentListOf() }
        knownState.update { persistentMapOf() }
        vectorsState.update { MixtapeVectors() }
        requestedVectorIds.clear()
        settledPicker = null
        typeState.update { type }
    }

    fun setQuery(query: String) {
        queryState.update { query }
        searchLoadingState.update { query.isNotBlank() }
    }

    fun pick(item: MixtapeItem) {
        val picks = picksState.value
        if (picks.size >= MAX_MIXTAPE_PICKS || picks.any { it.id == item.id }) return

        picksState.update { (it + item).toImmutableList() }
    }

    fun removePick(item: MixtapeItem) {
        picksState.update { picks -> picks.filterNot { it.id == item.id }.toImmutableList() }
    }

    fun clearPicks() {
        picksState.update { persistentListOf() }
    }

    private fun holdSettled(
        picker: MixtapePicker,
        settling: Boolean,
        historyLoading: Boolean,
    ): MixtapePicker {
        if (historyLoading) return picker
        if (!settling) {
            settledPicker = picker
            return picker
        }
        return settledPicker ?: picker
    }

    private fun MediaMode.toMixtapeType(): MediaType {
        return when (this) {
            MediaMode.Shows -> MediaType.Show
            MediaMode.Movies, MediaMode.Media -> MediaType.Movie
        }
    }

    val state = combine(
        typeState,
        queryState,
        picksState,
        historyState,
        historyLoadingState,
        searchResultsState,
        searchLoadingState,
        deepFitsState,
        deepLoadingState,
        knownState,
        vectorsState,
    ) { state ->
        val type = state[0] as MediaType
        val query = state[1] as String
        val picks = state[2] as ImmutableList<MixtapeItem>
        val historyLoading = state[4] as Boolean
        val searchLoading = state[6] as Boolean
        val deepLoading = state[8] as Boolean
        val known = state[9] as Map<TraktId, MixtapeItem>
        val pickIds = picks.map { it.id }

        val localFits = scoreFitsUseCase.scoreLocalFits(
            picks = pickIds,
            candidates = known.keys,
            vectors = state[10] as MixtapeVectors,
        )
        val fits = scoreFitsUseCase.mergeFits(
            local = localFits.orEmpty(),
            deep = state[7] as List<Fit>,
        )
        val picker = MixtapePicker.create(
            history = state[3] as List<MixtapeItem>,
            searchResults = state[5] as List<MixtapeItem>,
            known = known,
            picks = pickIds,
            fits = fits,
            query = query,
        )

        val settling = picker.kind == MixtapePicker.Kind.Fits &&
            localFits.isNullOrEmpty() &&
            deepLoading
        val shown = holdSettled(picker, settling, historyLoading)
        val searchPending = picker.kind == MixtapePicker.Kind.Search &&
            picker.items.isEmpty() &&
            searchLoading

        MixtapeState(
            type = type,
            picks = picks,
            picker = shown,
            loading = historyLoading || (settling && shown === picker) || searchPending,
            settling = settling,
        )
    }.flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5_000),
            initialValue = initialState,
        )
}

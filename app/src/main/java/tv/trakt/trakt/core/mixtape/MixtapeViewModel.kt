package tv.trakt.trakt.core.mixtape

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
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
import tv.trakt.trakt.core.mixtape.model.MixtapePickerHold
import tv.trakt.trakt.core.mixtape.navigation.MixtapeDestination
import tv.trakt.trakt.core.mixtape.usecases.GetMixtapeHistoryUseCase
import tv.trakt.trakt.core.mixtape.usecases.GetMixtapePickUseCase
import tv.trakt.trakt.core.mixtape.usecases.GetMixtapeUseCase
import tv.trakt.trakt.core.mixtape.usecases.GetMixtapeVectorsUseCase
import tv.trakt.trakt.core.mixtape.usecases.MixtapeHintUseCase
import tv.trakt.trakt.core.mixtape.usecases.ScoreMixtapeFitsUseCase
import kotlin.time.Duration.Companion.milliseconds

private val SEARCH_DEBOUNCE = 300.milliseconds
private const val OPEN_SOURCE = "navbar"
private const val PICK_SOURCE_GRID = "grid"
private const val PICK_SOURCE_SEARCH = "search"
private val DEEP_FITS_TIMEOUT = 1500.milliseconds

private data class DeepFits(
    val picks: List<TraktId> = emptyList(),
    val fits: List<Fit> = emptyList(),
)

@OptIn(FlowPreview::class)
@Suppress("UNCHECKED_CAST")
internal class MixtapeViewModel(
    savedStateHandle: SavedStateHandle,
    filterManager: GlobalFilterManager,
    private val analytics: Analytics,
    private val mixtapeHintUseCase: MixtapeHintUseCase,
    private val getHistoryUseCase: GetMixtapeHistoryUseCase,
    private val getVectorsUseCase: GetMixtapeVectorsUseCase,
    private val getMixtapeUseCase: GetMixtapeUseCase,
    private val scoreFitsUseCase: ScoreMixtapeFitsUseCase,
    private val getPickUseCase: GetMixtapePickUseCase,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<MixtapeDestination>()

    private val initialState = MixtapeState(
        type = destination.type ?: filterManager.getFilter().mode.toMixtapeType(),
    )

    private val typeState = MutableStateFlow(initialState.type)
    private val queryState = MutableStateFlow("")
    private val picksState = MutableStateFlow(initialState.picks)
    private val historyState = MutableStateFlow<ImmutableList<MixtapeItem>>(persistentListOf())
    private val historyLoadingState = MutableStateFlow(true)
    private val searchResultsState = MutableStateFlow<ImmutableList<MixtapeItem>>(persistentListOf())
    private val searchLoadingState = MutableStateFlow(false)
    private val deepFitsState = MutableStateFlow(DeepFits())
    private val deepTimedOutState = MutableStateFlow<List<TraktId>?>(null)
    private val knownState = MutableStateFlow<PersistentMap<TraktId, MixtapeItem>>(persistentMapOf())
    private val vectorsState = MutableStateFlow(MixtapeVectors())
    private val historyErrorState = MutableStateFlow<Exception?>(null)
    private val historyRetryState = MutableStateFlow(0)

    private val requestedVectorIds = mutableSetOf<TraktId>()
    private val pickerHold = MixtapePickerHold()
    private var reportedNoFits: Pair<MediaType, List<TraktId>>? = null

    init {
        dismissHint()
        observeHistory()
        observeSearch()
        observeDeepFits()
        destination.pick?.let { loadPick(TraktId(it)) }

        analytics.logScreenView(
            screenName = "mixtape",
        )
        analytics.mixtape.logOpen(
            source = destination.source ?: OPEN_SOURCE,
            type = initialState.type.value,
        )
    }

    private fun loadPick(id: TraktId) {
        viewModelScope.launch {
            try {
                val type = initialState.type
                val item = getPickUseCase.getPick(type, id) ?: return@launch
                if (typeState.value != type) return@launch

                picksState.update { picks ->
                    if (picks.any { it.id == item.id }) picks else (picks + item).toImmutableList()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun dismissHint() {
        viewModelScope.launch {
            mixtapeHintUseCase.dismissHint()
        }
    }

    private fun observeHistory() {
        viewModelScope.launch {
            combine(typeState, historyRetryState) { type, _ -> type }.collectLatest { type ->
                try {
                    historyLoadingState.update { true }
                    historyErrorState.update { null }
                    val history = getHistoryUseCase.getHistory(type)
                    historyState.update { history }
                    addKnown(history)
                    loadVectors(type, history.map { it.id })
                } catch (error: Exception) {
                    error.rethrowCancellation {
                        Timber.recordError(error)
                        historyErrorState.update { error }
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
                    if (picks.isEmpty()) return@collectLatest

                    val timeout = viewModelScope.launch {
                        delay(DEEP_FITS_TIMEOUT)
                        deepTimedOutState.update { picks }
                    }
                    try {
                        loadVectors(type, picks)
                        val mixtape = getMixtapeUseCase.getMixtape(
                            type = type,
                            picks = picks,
                            suggestions = false,
                        )
                        addKnown(mixtape.fits.mapNotNull { it.item })
                        deepFitsState.update { DeepFits(picks, mixtape.fits) }
                    } catch (error: Exception) {
                        error.rethrowCancellation {
                            Timber.recordError(error)
                            deepFitsState.update { DeepFits(picks) }
                        }
                    } finally {
                        timeout.cancel()
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

    fun retryHistory() {
        historyRetryState.update { it + 1 }
    }

    fun setType(type: MediaType) {
        if (type == typeState.value) return

        picksState.update { persistentListOf() }
        historyState.update { persistentListOf() }
        searchResultsState.update { persistentListOf() }
        deepFitsState.update { DeepFits() }
        deepTimedOutState.update { null }
        knownState.update { persistentMapOf() }
        vectorsState.update { MixtapeVectors() }
        requestedVectorIds.clear()
        pickerHold.reset()
        typeState.update { type }
    }

    fun setQuery(query: String) {
        queryState.update { query }
        searchLoadingState.update { query.isNotBlank() }
    }

    fun pick(item: MixtapeItem) {
        val picks = picksState.value
        if (picks.size >= MAX_MIXTAPE_PICKS || picks.any { it.id == item.id }) return

        analytics.mixtape.logPick(
            source = if (queryState.value.isBlank()) PICK_SOURCE_GRID else PICK_SOURCE_SEARCH,
            type = typeState.value.value,
            picks = picks.size + 1,
        )

        picksState.update { (it + item).toImmutableList() }
    }

    fun removePick(item: MixtapeItem) {
        picksState.update { picks -> picks.filterNot { it.id == item.id }.toImmutableList() }
    }

    fun logMix() {
        val picker = state.value.picker
        analytics.mixtape.logMix(
            type = typeState.value.value,
            picks = picksState.value.size,
            fits = if (picker.kind == MixtapePicker.Kind.Fits) picker.items.size else 0,
        )
    }

    fun logNoFits() {
        val current = state.value
        if (!current.isOutOfFits) return

        val key = current.type to current.picks.map { it.id }
        if (key == reportedNoFits) return
        reportedNoFits = key

        analytics.mixtape.logNoFits(
            type = current.type.value,
            picks = current.picks.size,
        )
    }

    fun clearPicks() {
        picksState.update { persistentListOf() }
    }

    private fun MediaMode.toMixtapeType(): MediaType {
        return when (this) {
            MediaMode.Shows, MediaMode.Media -> MediaType.Show
            MediaMode.Movies -> MediaType.Movie
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
        deepTimedOutState,
        knownState,
        vectorsState,
        historyErrorState,
    ) { state ->
        val type = state[0] as MediaType
        val query = state[1] as String
        val picks = state[2] as ImmutableList<MixtapeItem>
        val historyLoading = state[4] as Boolean
        val searchLoading = state[6] as Boolean
        val known = state[9] as Map<TraktId, MixtapeItem>
        val pickIds = picks.map { it.id }
        val deep = state[7] as DeepFits
        val deepReady = deep.picks == pickIds
        val deepTimedOut = state[8] == pickIds

        val localFits = scoreFitsUseCase.scoreLocalFits(
            picks = pickIds,
            candidates = known.keys,
            vectors = state[10] as MixtapeVectors,
        )
        val deepFits = if (deepReady) deep.fits else emptyList()
        val fits = when {
            deepTimedOut -> scoreFitsUseCase.appendFits(localFits.orEmpty(), deepFits)
            else -> scoreFitsUseCase.mergeFits(localFits.orEmpty(), deepFits)
        }
        val picker = MixtapePicker.create(
            history = state[3] as List<MixtapeItem>,
            searchResults = state[5] as List<MixtapeItem>,
            known = known,
            picks = pickIds,
            fits = fits,
            query = query,
        )

        val settling = picker.kind == MixtapePicker.Kind.Fits &&
            !deepReady &&
            (!deepTimedOut || localFits.isNullOrEmpty())
        val shown = pickerHold.resolve(picker, settling, historyLoading)
        val searchPending = picker.kind == MixtapePicker.Kind.Search &&
            picker.items.isEmpty() &&
            searchLoading

        MixtapeState(
            type = type,
            picks = picks,
            picker = shown,
            loading = historyLoading ||
                (settling && (shown === picker || shown.items.isEmpty())) ||
                searchPending,
            settling = settling,
            historyError = state[11] as Exception?,
        )
    }.flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = WhileSubscribed(5_000),
            initialValue = initialState,
        )
}

package tv.trakt.trakt.core.mixtape.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.mixtape.Mixtape.Suggestion
import tv.trakt.trakt.common.model.mixtape.MixtapeItem
import tv.trakt.trakt.core.mixtape.model.MixtapeTarget
import tv.trakt.trakt.core.mixtape.model.MixtapeTarget.PersonalList
import tv.trakt.trakt.core.mixtape.model.toMixtapeSlots
import tv.trakt.trakt.core.mixtape.navigation.MixtapeResultsDestination
import tv.trakt.trakt.core.mixtape.usecases.GetMixtapeListsUseCase
import tv.trakt.trakt.core.mixtape.usecases.GetMixtapeUseCase
import tv.trakt.trakt.core.mixtape.usecases.SaveMixtapeUseCase

private const val RESULT_OPEN = "open"
private const val RESULT_SKIP = "skip"
private const val RESULT_WATCHLIST = "watchlist"

private data class MixtapeMove(
    val from: MixtapeTarget,
    val to: MixtapeTarget,
    val items: List<MixtapeItem>,
    val added: List<MixtapeItem>,
)

@Suppress("UNCHECKED_CAST")
internal class MixtapeResultsViewModel(
    savedStateHandle: SavedStateHandle,
    private val analytics: Analytics,
    private val getMixtapeUseCase: GetMixtapeUseCase,
    private val getListsUseCase: GetMixtapeListsUseCase,
    private val saveUseCase: SaveMixtapeUseCase,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<MixtapeResultsDestination>()
    private val picks = destination.picks.map(::TraktId)

    private val initialState = MixtapeResultsState(
        type = destination.type,
        pickTitles = picks.zip(destination.titles).toMap().toImmutableMap(),
    )

    private val loadingState = MutableStateFlow(initialState.loading)
    private val suggestionsState = MutableStateFlow(initialState.suggestions)
    private val skipsState = MutableStateFlow<ImmutableList<TraktId>>(persistentListOf())
    private val roundsState = MutableStateFlow(initialState.rounds)
    private val savedToState = MutableStateFlow(initialState.savedTo)
    private val listsState = MutableStateFlow(initialState.lists)
    private val movingState = MutableStateFlow(initialState.moving)

    private val eventsFlow = MutableSharedFlow<MixtapeResultsEvent>(replay = 0)
    val events = eventsFlow.asSharedFlow()

    private var lastMove: MixtapeMove? = null

    init {
        loadMixtape()
        loadLists()
    }

    private fun loadMixtape() {
        viewModelScope.launch {
            try {
                val mixtape = getMixtapeUseCase.getMixtape(
                    type = destination.type,
                    picks = picks,
                    suggestions = true,
                )
                suggestionsState.update { mixtape.suggestions }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    private fun loadLists() {
        viewModelScope.launch {
            try {
                listsState.update { getListsUseCase.getLists().toImmutableList() }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun slots() = toMixtapeSlots(suggestionsState.value, skipsState.value)

    private fun position() = roundsState.value.lastOrNull()?.size ?: 0

    private fun kept() = roundsState.value.flatten()

    private fun logResult(action: String) {
        analytics.mixtape.logResult(
            action = action,
            position = position() + 1,
            type = destination.type.value,
        )
    }

    fun keep() {
        val slot = slots().getOrNull(position()) ?: return
        val target = savedToState.value
        logResult(RESULT_WATCHLIST)
        roundsState.update { rounds ->
            val current = rounds.lastOrNull().orEmpty()
            (rounds.dropLast(1) + listOf((current + slot.item).toImmutableList())).toImmutableList()
        }

        viewModelScope.launch {
            try {
                saveUseCase.keep(target, slot.item)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
                if (target is PersonalList) {
                    roundsState.update { rounds ->
                        rounds.map { round -> round.filterNot { it.id == slot.item.id }.toImmutableList() }
                            .toImmutableList()
                    }
                    eventsFlow.emit(MixtapeResultsEvent.KeepFailed(slot.item.title))
                }
            }
        }
    }

    fun skip() {
        val slot = slots().getOrNull(position()) ?: return
        logResult(RESULT_SKIP)
        skipsState.update { (it + slot.item.id).toImmutableList() }
    }

    fun open() {
        logResult(RESULT_OPEN)
    }

    fun findMore() {
        val ids = slots().map { it.item.id }
        skipsState.update { (it + ids).toImmutableList() }
        roundsState.update { (it + listOf(persistentListOf<MixtapeItem>())).toImmutableList() }
    }

    fun move(to: MixtapeTarget) {
        val from = savedToState.value
        if (from == to || movingState.value) return

        viewModelScope.launch {
            movingState.update { true }
            try {
                transfer(from = from, to = to)
            } finally {
                movingState.update { false }
            }
        }
    }

    fun moveToNewList(name: String) {
        if (name.isBlank() || movingState.value) return

        viewModelScope.launch {
            movingState.update { true }
            try {
                val list = getListsUseCase.createList(name)
                listsState.update { (it + list).toImmutableList() }
                transfer(from = savedToState.value, to = list)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
                eventsFlow.emit(MixtapeResultsEvent.MoveFailed)
            } finally {
                movingState.update { false }
            }
        }
    }

    private suspend fun transfer(
        from: MixtapeTarget,
        to: MixtapeTarget,
    ) {
        val items = kept()
        try {
            val added = saveUseCase.move(from = from, to = to, items = items)
            lastMove = MixtapeMove(from = from, to = to, items = items, added = added)
            savedToState.update { to }
            eventsFlow.emit(MixtapeResultsEvent.Moved(count = items.size, target = to))
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
            eventsFlow.emit(MixtapeResultsEvent.MoveFailed)
        }
    }

    fun undoMove() {
        val move = lastMove ?: return
        lastMove = null

        viewModelScope.launch {
            try {
                saveUseCase.undoMove(
                    from = move.from,
                    to = move.to,
                    items = move.items,
                    added = move.added,
                )
                savedToState.update { move.from }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
                eventsFlow.emit(MixtapeResultsEvent.MoveFailed)
            }
        }
    }

    val state = combine(
        loadingState,
        suggestionsState,
        skipsState,
        roundsState,
        savedToState,
        listsState,
        movingState,
    ) { state ->
        initialState.copy(
            loading = state[0] as Boolean,
            suggestions = state[1] as ImmutableList<Suggestion>,
            slots = toMixtapeSlots(
                suggestions = state[1] as List<Suggestion>,
                skips = state[2] as List<TraktId>,
            ),
            rounds = state[3] as ImmutableList<ImmutableList<MixtapeItem>>,
            savedTo = state[4] as MixtapeTarget,
            lists = state[5] as ImmutableList<PersonalList>,
            moving = state[6] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

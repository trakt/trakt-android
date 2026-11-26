package tv.trakt.trakt.core.mixtape.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
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
import tv.trakt.trakt.core.mixtape.model.toMixtapeSlots
import tv.trakt.trakt.core.mixtape.navigation.MixtapeResultsDestination
import tv.trakt.trakt.core.mixtape.usecases.AddMixtapeToWatchlistUseCase
import tv.trakt.trakt.core.mixtape.usecases.GetMixtapeUseCase

private const val RESULT_OPEN = "open"
private const val RESULT_SKIP = "skip"
private const val RESULT_WATCHLIST = "watchlist"

@Suppress("UNCHECKED_CAST")
internal class MixtapeResultsViewModel(
    savedStateHandle: SavedStateHandle,
    private val analytics: Analytics,
    private val getMixtapeUseCase: GetMixtapeUseCase,
    private val addToWatchlistUseCase: AddMixtapeToWatchlistUseCase,
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

    init {
        loadMixtape()
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

    private fun slots() = toMixtapeSlots(suggestionsState.value, skipsState.value)

    private fun position() = roundsState.value.lastOrNull()?.size ?: 0

    private fun logResult(action: String) {
        analytics.mixtape.logResult(
            action = action,
            position = position() + 1,
            type = destination.type.value,
        )
    }

    fun keep() {
        val slot = slots().getOrNull(position()) ?: return
        logResult(RESULT_WATCHLIST)
        roundsState.update { rounds ->
            val current = rounds.lastOrNull().orEmpty()
            (rounds.dropLast(1) + listOf((current + slot.item).toImmutableList())).toImmutableList()
        }

        viewModelScope.launch {
            try {
                addToWatchlistUseCase.addToWatchlist(slot.item)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
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

    val state = combine(
        loadingState,
        suggestionsState,
        skipsState,
        roundsState,
    ) { state ->
        initialState.copy(
            loading = state[0] as Boolean,
            suggestions = state[1] as ImmutableList<Suggestion>,
            slots = toMixtapeSlots(
                suggestions = state[1] as List<Suggestion>,
                skips = state[2] as List<TraktId>,
            ),
            rounds = state[3] as ImmutableList<ImmutableList<MixtapeItem>>,
        )
    }.stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

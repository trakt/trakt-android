package tv.trakt.trakt.core.home.sections.recommended.whythis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.data.remote.MediaRemoteSource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.recommended.RecommendedItem
import tv.trakt.trakt.common.model.recommended.RecommendedPerson
import tv.trakt.trakt.common.model.recommended.RecommendedSource

@Suppress("UNCHECKED_CAST")
internal class WhyThisViewModel(
    private val item: RecommendedItem,
    private val remoteSource: MediaRemoteSource,
) : ViewModel() {
    private val initialState = WhyThisState(sources = item.sources)

    private val sourcesState = MutableStateFlow(initialState.sources)
    private val peopleState = MutableStateFlow(initialState.people)
    private val loadingState = MutableStateFlow(initialState.loading)

    init {
        if (item.sources.isEmpty()) {
            loadReasons()
        }
    }

    private fun loadReasons() {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }

                val reasons = remoteSource.getRecommendationReasons(
                    type = item.type,
                    mediaId = item.id,
                )

                sourcesState.update {
                    reasons.sources.orEmpty()
                        .map(RecommendedSource::fromDto)
                        .toImmutableList()
                }
                peopleState.update {
                    reasons.people.orEmpty()
                        .map(RecommendedPerson::fromDto)
                        .toImmutableList()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    val state = combine(
        sourcesState,
        peopleState,
        loadingState,
    ) { state ->
        WhyThisState(
            sources = state[0] as ImmutableList<RecommendedSource>,
            people = state[1] as ImmutableList<RecommendedPerson>,
            loading = state[2] as LoadingState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

package tv.trakt.trakt.core.lists.features.details.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.lists.features.details.info.usecases.GetListFeaturedPeopleUseCase

@Suppress("UNCHECKED_CAST")
internal class ListInfoViewModel(
    private val listId: TraktId,
    private val getFeaturedPeopleUseCase: GetListFeaturedPeopleUseCase,
    analytics: Analytics,
) : ViewModel() {
    private val initialState = ListInfoState()

    private val peopleState = MutableStateFlow(initialState.people)
    private val loadingState = MutableStateFlow(initialState.loading)

    init {
        loadData()
        analytics.logDrilldown(source = "list-details", type = null)
    }

    private fun loadData() {
        viewModelScope.launch {
            loadingState.update { Loading }
            try {
                peopleState.update {
                    getFeaturedPeopleUseCase.getPeople(listId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    peopleState.update { persistentListOf() }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    val state = combine(
        peopleState,
        loadingState,
    ) { state ->
        ListInfoState(
            people = state[0] as ImmutableList<Person>?,
            loading = state[1] as LoadingState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

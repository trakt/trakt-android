package tv.trakt.trakt.core.lists.sections.smart.details.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.lists.AwardCeremony
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.core.lists.features.smart.usecase.GetSmartListFilterOptionsUseCase

@Suppress("UNCHECKED_CAST")
internal class SmartListRecipeViewModel(
    private val list: SmartList,
    private val getFilterOptionsUseCase: GetSmartListFilterOptionsUseCase,
) : ViewModel() {
    private val initialState = SmartListRecipeState()

    private val awardCeremoniesState = MutableStateFlow(initialState.awardCeremonies)

    init {
        loadAwardCeremonies()
    }

    private fun loadAwardCeremonies() {
        if (list.filters.awards.isNullOrEmpty()) {
            return
        }

        viewModelScope.launch {
            try {
                awardCeremoniesState.update { getFilterOptionsUseCase.getAwardCatalog() }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.w(error, "Failed to load award catalog")
                }
            }
        }
    }

    val state = combine(
        awardCeremoniesState,
    ) { state ->
        SmartListRecipeState(
            awardCeremonies = state[0] as ImmutableList<AwardCeremony>,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

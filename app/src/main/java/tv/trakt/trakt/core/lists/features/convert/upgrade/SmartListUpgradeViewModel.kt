package tv.trakt.trakt.core.lists.features.convert.upgrade

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.lists.SmartListDraft
import tv.trakt.trakt.core.lists.features.convert.usecases.GetConvertedSmartListUseCase
import tv.trakt.trakt.core.lists.features.convert.usecases.GetSmartListDraftUseCase
import tv.trakt.trakt.core.lists.features.convert.usecases.SmartListDismissalsUseCase

private const val BANNER_ID_PREFIX = "smart-list-upgrade-"
private const val BANNER_DISMISSED_VALUE = "dismissed"

@Suppress("UNCHECKED_CAST")
internal class SmartListUpgradeViewModel(
    private val list: CustomList,
    private val getSmartListDraftUseCase: GetSmartListDraftUseCase,
    private val getConvertedSmartListUseCase: GetConvertedSmartListUseCase,
    private val dismissalsUseCase: SmartListDismissalsUseCase,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = SmartListUpgradeState()

    private val visibleState = MutableStateFlow(initialState.visible)
    private val draftState = MutableStateFlow(initialState.draft)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val navigateSmartListState = MutableStateFlow(initialState.navigateSmartList)

    init {
        val convertedTo = list.convertedTo
        when {
            convertedTo != null -> loadConvertedList(convertedTo.trakt)
            list.staleOrigin != null -> loadDraft()
        }
    }

    private fun loadConvertedList(smartListId: TraktId) {
        viewModelScope.launch {
            try {
                val smartList = getConvertedSmartListUseCase.getSmartList(smartListId)
                navigateSmartListState.update { smartList }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun loadDraft() {
        viewModelScope.launch {
            try {
                if (dismissalsUseCase.isUpgradeDismissed(list.ids.trakt)) {
                    return@launch
                }

                visibleState.update { true }
                loadingState.update { Loading }

                val draft = getSmartListDraftUseCase.getDraft(list.ids.trakt)
                draftState.update { draft }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    visibleState.update { false }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    fun dismiss() {
        visibleState.update { false }
        viewModelScope.launch {
            try {
                dismissalsUseCase.dismissUpgrade(list.ids.trakt)
                analytics.logBannerDismiss(
                    id = "$BANNER_ID_PREFIX${list.ids.trakt.value}",
                    value = BANNER_DISMISSED_VALUE,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun clearNavigation() {
        navigateSmartListState.update { null }
    }

    val state = combine(
        visibleState,
        draftState,
        loadingState,
        navigateSmartListState,
    ) { state ->
        SmartListUpgradeState(
            visible = state[0] as Boolean,
            draft = state[1] as SmartListDraft?,
            loading = state[2] as LoadingState,
            navigateSmartList = state[3] as SmartList?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

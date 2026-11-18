package tv.trakt.trakt.app.core.discover.features.media

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tv.trakt.trakt.app.core.discover.DiscoverConfig.DISCOVER_PAGE_LIMIT
import tv.trakt.trakt.app.core.discover.features.media.navigation.DiscoverMediaDestination
import tv.trakt.trakt.app.core.discover.usecase.GetDiscoverMediaUseCase
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.discover.DiscoverItem

@Suppress("UNCHECKED_CAST")
internal class DiscoverMediaViewAllViewModel(
    savedStateHandle: SavedStateHandle,
    private val getItemsUseCase: GetDiscoverMediaUseCase,
    private val collectionStateProvider: CollectionStateProvider,
    private val analytics: Analytics,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<DiscoverMediaDestination>()
    private val initialState = DiscoverMediaViewAllState(section = destination.section)

    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val loadingPageState = MutableStateFlow(initialState.isLoadingPage)
    private val itemsState = MutableStateFlow(initialState.items)
    private val errorState = MutableStateFlow(initialState.error)

    private var nextDataPage: Int = 1
    private var hasMoreData: Boolean = true

    init {
        loadData()

        collectionStateProvider
            .launchIn(viewModelScope)

        analytics.logScreenView(
            screenName = destination.section.screenName,
        )
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { true }

                val items = getItemsUseCase.getItems(
                    section = destination.section,
                    limit = DISCOVER_PAGE_LIMIT,
                    page = 1,
                )
                itemsState.update { items }

                hasMoreData = items.size >= DISCOVER_PAGE_LIMIT
                nextDataPage += 1
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    fun loadNextDataPage() {
        if (loadingPageState.value || !hasMoreData) {
            return
        }
        viewModelScope.launch {
            try {
                loadingPageState.update { true }

                val items = getItemsUseCase.getItems(
                    section = destination.section,
                    limit = DISCOVER_PAGE_LIMIT,
                    page = nextDataPage,
                )

                itemsState.update { current ->
                    current
                        ?.plus(items)
                        ?.distinctBy { it.key }
                        ?.toImmutableList()
                }

                hasMoreData = items.size >= DISCOVER_PAGE_LIMIT
                nextDataPage += 1
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                }
            } finally {
                loadingPageState.update { false }
            }
        }
    }

    val state = combine(
        loadingState,
        loadingPageState,
        itemsState,
        collectionStateProvider.stateFlow,
        errorState,
    ) { state ->
        DiscoverMediaViewAllState(
            section = destination.section,
            isLoading = state[0] as Boolean,
            isLoadingPage = state[1] as Boolean,
            items = state[2] as ImmutableList<DiscoverItem>?,
            collection = state[3] as UserCollectionState,
            error = state[4] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

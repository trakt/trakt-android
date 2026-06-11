package tv.trakt.trakt.core.userprofile.sections.favorites

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
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.sorting.Sorting
import tv.trakt.trakt.core.favorites.model.FavoriteItem
import tv.trakt.trakt.core.user.CollectionStateProvider
import tv.trakt.trakt.core.user.UserCollectionState
import tv.trakt.trakt.core.userprofile.UserProfileConfig.FAVORITES_SECTION_LIMIT
import tv.trakt.trakt.core.userprofile.sections.favorites.usecases.GetUserProfileFavoritesUseCase

internal class UserProfileFavoritesViewModel(
    private val userId: TraktId,
    private val getFavoritesUseCase: GetUserProfileFavoritesUseCase,
    private val collectionStateProvider: CollectionStateProvider,
) : ViewModel() {
    private val initialState = UserProfileFavoritesState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
        observeData()
    }

    private fun observeData() {
        collectionStateProvider
            .launchIn(viewModelScope)
    }

    fun loadData(ignoreErrors: Boolean = false) {
        viewModelScope.launch {
            try {
                if (itemsState.value == initialState.items) {
                    loadingState.update { Loading }
                }

                itemsState.update {
                    getFavoritesUseCase.getUserFavorites(
                        userId = userId,
                        sorting = Sorting.RecentlyAdded,
                    )
                        .take(FAVORITES_SECTION_LIMIT)
                        .toImmutableList()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        itemsState,
        loadingState,
        errorState,
        collectionStateProvider.stateFlow,
    ) { state ->
        UserProfileFavoritesState(
            items = state[0] as ImmutableList<FavoriteItem>?,
            loading = state[1] as LoadingState,
            error = state[2] as Exception?,
            collection = state[3] as UserCollectionState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

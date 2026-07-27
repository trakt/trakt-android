package tv.trakt.trakt.app.core.profile.sections.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.app.Config.REFRESH_DATA_THRESHOLD_MINUTES
import tv.trakt.trakt.app.core.profile.ProfileConfig.PROFILE_FAVORITES_SECTION_LIMIT
import tv.trakt.trakt.app.core.profile.sections.favorites.model.interleaveFavorites
import tv.trakt.trakt.app.core.profile.sections.favorites.usecases.GetFavoriteMoviesUseCase
import tv.trakt.trakt.app.core.profile.sections.favorites.usecases.GetFavoriteShowsUseCase
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.core.user.UserCollectionState
import tv.trakt.trakt.common.helpers.extensions.nowUtc
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider.State.FOREGROUND
import java.time.ZonedDateTime

internal class ProfileFavoritesViewModel(
    private val getFavoriteShowsCase: GetFavoriteShowsUseCase,
    private val getFavoriteMoviesCase: GetFavoriteMoviesUseCase,
    private val collectionStateProvider: CollectionStateProvider,
    private val appLifecycleProvider: AppLifecycleProvider,
) : ViewModel() {
    private val initialState = ProfileFavoritesState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.isLoading)
    private val errorState = MutableStateFlow(initialState.error)

    private var loadedAt: ZonedDateTime? = null

    init {
        loadData()
        observeApp()
        observeData()
    }

    private fun observeData() {
        collectionStateProvider
            .launchIn(viewModelScope)
    }

    private fun observeApp() {
        appLifecycleProvider.observeState(FOREGROUND)
            .filter {
                loadedAt != null &&
                    nowUtc().minusMinutes(REFRESH_DATA_THRESHOLD_MINUTES).isAfter(loadedAt)
            }
            .onEach {
                loadData(showLoading = false)
            }
            .launchIn(viewModelScope)
    }

    private fun loadData(showLoading: Boolean = true) {
        viewModelScope.launch {
            try {
                if (showLoading) {
                    loadingState.update { true }
                }

                itemsState.update {
                    coroutineScope {
                        val shows = async {
                            getFavoriteShowsCase.getFavoriteShows(limit = PROFILE_FAVORITES_SECTION_LIMIT)
                        }
                        val movies = async {
                            getFavoriteMoviesCase.getFavoriteMovies(limit = PROFILE_FAVORITES_SECTION_LIMIT)
                        }
                        interleaveFavorites(shows.await(), movies.await())
                            .take(PROFILE_FAVORITES_SECTION_LIMIT)
                            .toImmutableList()
                    }
                }

                loadedAt = nowUtc()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error, "Failed to load data")
                    errorState.update { error }
                }
            } finally {
                loadingState.update { false }
            }
        }
    }

    val state: StateFlow<ProfileFavoritesState> = combine(
        loadingState,
        itemsState,
        collectionStateProvider.stateFlow,
        errorState,
    ) { loading, items, _, error ->
        ProfileFavoritesState(
            isLoading = loading,
            items = items,
            collection = UserCollectionState.Default,
            error = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

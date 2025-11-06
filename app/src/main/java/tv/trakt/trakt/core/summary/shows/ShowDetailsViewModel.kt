package tv.trakt.trakt.core.summary.shows

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.isNowOrBefore
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.MediaType.SHOW
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.favorites.FavoritesUpdates
import tv.trakt.trakt.core.favorites.FavoritesUpdates.Source.DETAILS
import tv.trakt.trakt.core.lists.sections.personal.usecases.AddPersonalListItemUseCase
import tv.trakt.trakt.core.lists.sections.personal.usecases.RemovePersonalListItemUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.main.usecases.HalloweenUseCase
import tv.trakt.trakt.core.profile.model.FavoriteItem
import tv.trakt.trakt.core.ratings.data.work.PostRatingWorker
import tv.trakt.trakt.core.summary.shows.ShowDetailsState.UserRatingsState
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source
import tv.trakt.trakt.core.summary.shows.navigation.ShowDetailsDestination
import tv.trakt.trakt.core.summary.shows.usecases.GetShowDetailsUseCase
import tv.trakt.trakt.core.summary.shows.usecases.GetShowRatingsUseCase
import tv.trakt.trakt.core.summary.shows.usecases.GetShowStudiosUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowFavoritesUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowWatchlistUseCase
import tv.trakt.trakt.core.user.data.local.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.data.local.favorites.UserFavoritesLocalDataSource
import tv.trakt.trakt.core.user.usecases.lists.LoadUserFavoritesUseCase
import tv.trakt.trakt.core.user.usecases.lists.LoadUserListsUseCase
import tv.trakt.trakt.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.core.user.usecases.ratings.LoadUserRatingsUseCase
import tv.trakt.trakt.resources.R
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
internal class ShowDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val appContext: Context,
    private val getDetailsUseCase: GetShowDetailsUseCase,
    private val getExternalRatingsUseCase: GetShowRatingsUseCase,
    private val getShowStudiosUseCase: GetShowStudiosUseCase,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val loadWatchlistUseCase: LoadUserWatchlistUseCase,
    private val loadListsUseCase: LoadUserListsUseCase,
    private val loadRatingUseCase: LoadUserRatingsUseCase,
    private val loadFavoritesUseCase: LoadUserFavoritesUseCase,
    private val updateShowHistoryUseCase: UpdateShowHistoryUseCase,
    private val updateEpisodeHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val updateShowWatchlistUseCase: UpdateShowWatchlistUseCase,
    private val updateShowFavoritesUseCase: UpdateShowFavoritesUseCase,
    private val addListItemUseCase: AddPersonalListItemUseCase,
    private val removeListItemUseCase: RemovePersonalListItemUseCase,
    private val halloweenUseCase: HalloweenUseCase,
    private val userWatchlistLocalSource: UserWatchlistLocalDataSource,
    private val userFavoritesLocalSource: UserFavoritesLocalDataSource,
    private val episodeLocalDataSource: EpisodeLocalDataSource,
    private val showDetailsUpdates: ShowDetailsUpdates,
    private val favoritesUpdates: FavoritesUpdates,
    private val sessionManager: SessionManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<ShowDetailsDestination>()
    private val showId = destination.showId.toTraktId()

    private val initialState = ShowDetailsState()

    private val showState = MutableStateFlow(initialState.show)
    private val showRatingsState = MutableStateFlow(initialState.showRatings)
    private val showUserRatingsState = MutableStateFlow(initialState.showUserRating)
    private val showStudiosState = MutableStateFlow(initialState.showStudios)
    private val showProgressState = MutableStateFlow(initialState.showProgress)
    private val navigateEpisode = MutableStateFlow(initialState.navigateEpisode)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingProgress = MutableStateFlow(initialState.loadingProgress)
    private val loadingLists = MutableStateFlow(initialState.loadingLists)
    private val loadingFavorite = MutableStateFlow(initialState.loadingFavorite)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)
    private val userState = MutableStateFlow(initialState.user)
    private val halloweenState = MutableStateFlow(initialState.halloween)

    private var ratingJob: kotlinx.coroutines.Job? = null

    init {
        loadUser()
        loadData()
        loadProgressData()
        loadUserRatingData()
        observeData()

        analytics.logScreenView(
            screenName = "show_details",
        )
    }

    private fun observeData() {
        showDetailsUpdates.observeUpdates(Source.SEASONS)
            .distinctUntilChanged()
            .debounce(200)
            .onEach {
                loadProgressData(
                    ignoreErrors = true,
                )
            }
            .launchIn(viewModelScope)
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                userState.update {
                    sessionManager.getProfile()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error)
                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                var show = getDetailsUseCase.getLocalShow(showId)
                if (show == null) {
                    loadingState.update { LOADING }
                    show = getDetailsUseCase.getShow(
                        showId = showId,
                    )
                }
                showState.update { show }

                loadRatings(show)
                loadStudios()
                loadHalloween(show)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error)
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    private fun loadHalloween(show: Show?) {
        viewModelScope.launch {
            delay(100)
            try {
                halloweenState.update {
                    show?.genres?.contains("horror") == true &&
                        halloweenUseCase.getConfig().enabled
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error)
                }
            }
        }
    }

    private fun loadProgressData(ignoreErrors: Boolean = false) {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingProgress.update { LOADING }
                loadingLists.update { LOADING }

                coroutineScope {
                    val progressAsync = async {
                        if (!loadProgressUseCase.isShowsLoaded()) {
                            loadProgressUseCase.loadShowsProgress()
                        }
                    }

                    val watchlistAsync = async {
                        if (!loadWatchlistUseCase.isShowsLoaded()) {
                            loadWatchlistUseCase.loadWatchlist()
                        }
                    }

                    val listsAsync = async {
                        if (!loadListsUseCase.isLoaded()) {
                            loadListsUseCase.loadLists()
                        }
                    }

                    progressAsync.await()
                    watchlistAsync.await()
                    listsAsync.await()
                }

                coroutineScope {
                    val progressAsync = async {
                        loadProgressUseCase.loadLocalShows()
                            .firstOrNull {
                                it.show.ids.trakt == showId
                            }
                    }

                    val watchlistAsync = async {
                        loadWatchlistUseCase.loadLocalShows()
                            .firstOrNull {
                                it.show.ids.trakt == showId
                            }
                    }

                    val listsAsync = async {
                        val lists = loadListsUseCase.loadLocalLists()
                        lists.size to lists.values.flatten()
                            .firstOrNull {
                                it.type == SHOW && it.id == showId
                            }
                    }

                    val progress = progressAsync.await()
                    val watchlist = watchlistAsync.await()
                    val (listsCount, lists) = listsAsync.await()

                    showProgressState.update {
                        ShowDetailsState.ProgressState(
                            aired = progress?.progress?.aired ?: 0,
                            plays = progress?.progress?.plays,
                            inWatchlist = watchlist != null,
                            inLists = lists != null,
                            hasLists = listsCount > 0,
                        )
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.e(error)
                }
            } finally {
                loadingProgress.update { DONE }
                loadingLists.update { DONE }
            }
        }
    }

    private fun loadRatings(show: Show?) {
        if (show?.released?.isNowOrBefore() != true) {
            // Don't load ratings for unreleased shows
            return
        }
        viewModelScope.launch {
            try {
                showRatingsState.update {
                    getExternalRatingsUseCase.getExternalRatings(showId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error)
                }
            }
        }
    }

    private fun loadStudios() {
        viewModelScope.launch {
            try {
                showStudiosState.update {
                    getShowStudiosUseCase.getStudios(showId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error)
                }
            }
        }
    }

    private fun loadUserRatingData() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                showUserRatingsState.update {
                    UserRatingsState(
                        loading = LOADING,
                    )
                }

                coroutineScope {
                    val ratingsAsync = async {
                        if (!loadRatingUseCase.isShowsLoaded()) {
                            loadRatingUseCase.loadShows()
                        }
                    }

                    val favoritesAsync = async {
                        if (!loadFavoritesUseCase.isShowsLoaded()) {
                            loadFavoritesUseCase.loadShows()
                        }
                    }

                    ratingsAsync.await()
                    favoritesAsync.await()
                }

                val userRatings = loadRatingUseCase.loadLocalShows()
                val userFavorites = loadFavoritesUseCase.loadLocalShows()
                    .associateBy { it.show.ids.trakt }

                val userRating = userRatings[showId]?.copy(
                    favorite = userFavorites[showId] != null,
                )

                showUserRatingsState.update {
                    UserRatingsState(
                        rating = userRating,
                        loading = DONE,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.e(error)
                }
            }
        }
    }

    fun toggleWatchlist() {
        if (showState.value == null ||
            loadingState.value.isLoading ||
            loadingProgress.value.isLoading ||
            loadingLists.value.isLoading
        ) {
            return
        }

        if (showProgressState.value?.inWatchlist == true) {
            removeFromWatchlist()
        } else {
            addToWatchlist()
        }
    }

    fun toggleList(
        listId: TraktId,
        add: Boolean,
    ) {
        if (showState.value == null ||
            loadingState.value.isLoading ||
            loadingProgress.value.isLoading ||
            loadingLists.value.isLoading
        ) {
            return
        }

        when {
            add -> addToList(listId)
            else -> removeFromList(listId)
        }
    }

    fun addToWatched() {
        if (showState.value == null ||
            loadingState.value.isLoading ||
            loadingProgress.value.isLoading ||
            loadingLists.value.isLoading
        ) {
            return
        }

        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingProgress.update { LOADING }

                updateShowHistoryUseCase.addToWatched(showId)
                val progress = loadProgressUseCase.loadShowsProgress()
                    .firstOrNull {
                        it.show.ids.trakt == showId
                    }
                userWatchlistLocalSource.removeShows(
                    ids = setOf(showId),
                    notify = true,
                )

                showProgressState.update {
                    it?.copy(
                        aired = progress?.progress?.aired ?: 0,
                        plays = progress?.progress?.plays ?: 0,
                        inWatchlist = false,
                    )
                }

                showDetailsUpdates.notifyUpdate(Source.PROGRESS)

                infoState.update {
                    DynamicStringResource(R.string.text_info_history_added)
                }
                analytics.progress.logAddWatchedMedia(
                    mediaType = "show",
                    source = "show_details",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error)
                }
            } finally {
                loadingProgress.update { DONE }
            }
        }
    }

    fun removeFromWatched(playId: Long) {
        if (showState.value == null ||
            loadingState.value.isLoading ||
            loadingProgress.value.isLoading ||
            loadingLists.value.isLoading
        ) {
            return
        }

        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingProgress.update { LOADING }

                updateEpisodeHistoryUseCase.removePlayFromHistory(playId)
                val progress = loadProgressUseCase.loadShowsProgress()
                    .firstOrNull {
                        it.show.ids.trakt == showId
                    }

                showProgressState.update {
                    it?.copy(
                        aired = progress?.progress?.aired ?: 0,
                        plays = progress?.progress?.plays ?: 0,
                    )
                }

                showDetailsUpdates.notifyUpdate(Source.PROGRESS)
                infoState.update {
                    DynamicStringResource(R.string.text_info_history_removed)
                }
                analytics.progress.logRemoveWatchedMedia(
                    mediaType = "episode",
                    source = "show_details",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error)
                }
            } finally {
                loadingProgress.update { DONE }
            }
        }
    }

    private fun addToWatchlist() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingLists.update { LOADING }

                updateShowWatchlistUseCase.addToWatchlist(showId)
                userWatchlistLocalSource.addShows(
                    shows = listOf(
                        WatchlistItem.ShowItem(
                            rank = 0,
                            show = showState.value!!,
                            listedAt = nowUtcInstant(),
                        ),
                    ),
                    notify = true,
                )

                showProgressState.update {
                    it?.copy(
                        inWatchlist = true,
                    )
                }

                infoState.update {
                    DynamicStringResource(R.string.text_info_watchlist_added)
                }
                analytics.progress.logAddWatchlistMedia(
                    mediaType = "show",
                    source = "show_details",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error)
                }
            } finally {
                loadingLists.update { DONE }
            }
        }
    }

    private fun removeFromWatchlist() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingLists.update { LOADING }

                updateShowWatchlistUseCase.removeFromWatchlist(showId)
                userWatchlistLocalSource.removeShows(
                    ids = setOf(showId),
                    notify = true,
                )

                refreshLists()

                infoState.update {
                    DynamicStringResource(R.string.text_info_watchlist_removed)
                }
                analytics.progress.logRemoveWatchlistMedia(
                    mediaType = "show",
                    source = "show_details",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error)
                }
            } finally {
                loadingLists.update { DONE }
            }
        }
    }

    private fun addToList(listId: TraktId) {
        viewModelScope.launch {
            val show = showState.value
            if (!sessionManager.isAuthenticated() || show == null) {
                return@launch
            }
            try {
                loadingLists.update { LOADING }

                addListItemUseCase.addShow(
                    listId = listId,
                    show = show,
                )

                showProgressState.update {
                    it?.copy(inLists = true)
                }
                infoState.update {
                    DynamicStringResource(R.string.text_info_list_added)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error)
                }
            } finally {
                loadingLists.update { DONE }
            }
        }
    }

    private fun removeFromList(listId: TraktId) {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingLists.update { LOADING }

                removeListItemUseCase.removeShow(
                    listId = listId,
                    showId = showId,
                )

                refreshLists()
                infoState.update {
                    DynamicStringResource(R.string.text_info_list_removed)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error)
                }
            } finally {
                loadingLists.update { DONE }
            }
        }
    }

    private suspend fun refreshLists() {
        return coroutineScope {
            val watchlistAsync = async {
                loadWatchlistUseCase.loadLocalShows()
                    .firstOrNull {
                        it.show.ids.trakt == showId
                    }
            }

            val listsAsync = async {
                val lists = loadListsUseCase.loadLocalLists()
                lists.size to lists.values.flatten()
                    .firstOrNull {
                        it.type == SHOW && it.id == showId
                    }
            }

            val watchlist = watchlistAsync.await()
            val (listsCount, lists) = listsAsync.await()

            showProgressState.update {
                it?.copy(
                    inWatchlist = watchlist != null,
                    inLists = lists != null,
                    hasLists = listsCount > 0,
                )
            }
        }
    }

    fun navigateToEpisode(
        show: Show,
        episode: Episode,
    ) {
        if (navigateEpisode.value != null ||
            loadingState.value.isLoading ||
            loadingProgress.value.isLoading ||
            loadingLists.value.isLoading
        ) {
            return
        }

        viewModelScope.launch {
            episodeLocalDataSource.upsertEpisodes(listOf(episode))

            navigateEpisode.update {
                Pair(show.ids.trakt, episode)
            }
        }
    }

    fun clearInfo() {
        infoState.update { null }
    }

    fun clearNavigation() {
        navigateEpisode.update { null }
    }

    fun toggleFavorite(add: Boolean) {
        if (showState.value == null ||
            loadingFavorite.value.isLoading
        ) {
            return
        }

        when {
            add -> addToFavorites()
            else -> removeFromFavorites()
        }
    }

    private fun addToFavorites() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingFavorite.update { LOADING }

                delay(300) // Small delay to allow UI to settle.
                updateShowFavoritesUseCase.addToFavorites(showId)
                userFavoritesLocalSource.addShows(
                    shows = listOf(
                        FavoriteItem.ShowItem(
                            rank = 0,
                            show = showState.value!!,
                            listedAt = nowUtcInstant(),
                        ),
                    ),
                )
                favoritesUpdates.notifyUpdate(DETAILS)

                showUserRatingsState.update {
                    it?.copy(
                        rating = it.rating?.copy(favorite = true),
                    )
                }

                analytics.ratings.logFavoriteAdd(mediaType = "show")
                infoState.update {
                    DynamicStringResource(R.string.text_info_favorites_added)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error)
                }
            } finally {
                loadingFavorite.update { DONE }
            }
        }
    }

    private fun removeFromFavorites() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingFavorite.update { LOADING }

                delay(300) // Small delay to allow UI to settle.
                updateShowFavoritesUseCase.removeFromFavorites(showId)
                userFavoritesLocalSource.removeShows(setOf(showId))
                favoritesUpdates.notifyUpdate(DETAILS)

                showUserRatingsState.update {
                    it?.copy(
                        rating = it.rating?.copy(
                            favorite = false,
                        ),
                    )
                }

                infoState.update {
                    DynamicStringResource(R.string.text_info_favorites_removed)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.e(error)
                }
            } finally {
                loadingFavorite.update { DONE }
            }
        }
    }

    fun addRating(newRating: Int) {
        ratingJob?.cancel()
        ratingJob = viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }

            showUserRatingsState.update {
                UserRatingsState(
                    rating = UserRating(
                        mediaId = showId,
                        mediaType = SHOW,
                        rating = newRating,
                        favorite = it?.rating?.favorite == true,
                    ),
                    loading = DONE,
                )
            }

            // Debounce to avoid multiple rapid calls.
            delay(2.seconds)
            PostRatingWorker.scheduleOneTime(
                appContext = appContext,
                mediaId = showId,
                mediaType = SHOW,
                rating = newRating,
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<ShowDetailsState> = combine(
        showState,
        showRatingsState,
        showUserRatingsState,
        showStudiosState,
        showProgressState,
        navigateEpisode,
        loadingState,
        loadingProgress,
        loadingLists,
        loadingFavorite,
        infoState,
        errorState,
        userState,
        halloweenState,
    ) { state ->
        ShowDetailsState(
            show = state[0] as Show?,
            showRatings = state[1] as ExternalRating?,
            showUserRating = state[2] as UserRatingsState?,
            showStudios = state[3] as ImmutableList<String>?,
            showProgress = state[4] as ShowDetailsState.ProgressState?,
            navigateEpisode = state[5] as Pair<TraktId, Episode>?,
            loading = state[6] as LoadingState,
            loadingProgress = state[7] as LoadingState,
            loadingLists = state[8] as LoadingState,
            loadingFavorite = state[9] as LoadingState,
            info = state[10] as StringResource?,
            error = state[11] as Exception?,
            user = state[12] as User?,
            halloween = state[13] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

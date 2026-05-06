package tv.trakt.trakt.core.summary.shows

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.translations.model.MediaTranslation
import tv.trakt.trakt.common.core.translations.usecase.GetShowTranslationsUseCase
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.firebase.inappreview.RequestAppReviewUseCase
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.isNowOrBefore
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.ExternalRating
import tv.trakt.trakt.common.model.MediaType.SHOW
import tv.trakt.trakt.common.model.Person
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.favorites.FavoritesUpdates
import tv.trakt.trakt.core.favorites.FavoritesUpdates.Source.DETAILS
import tv.trakt.trakt.core.favorites.model.FavoriteItem
import tv.trakt.trakt.core.lists.sections.personal.usecases.manage.AddPersonalListItemUseCase
import tv.trakt.trakt.core.lists.sections.personal.usecases.manage.RemovePersonalListItemUseCase
import tv.trakt.trakt.core.lists.sections.watchlist.model.WatchlistItem
import tv.trakt.trakt.core.ratings.data.work.PostRatingWorker
import tv.trakt.trakt.core.summary.shows.ShowDetailsState.UserRatingsState
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source
import tv.trakt.trakt.core.summary.shows.features.actors.usecases.GetShowCreatorUseCase
import tv.trakt.trakt.core.summary.shows.navigation.ShowDetailsDestination
import tv.trakt.trakt.core.summary.shows.usecases.GetShowDetailsUseCase
import tv.trakt.trakt.core.summary.shows.usecases.GetShowRatingsUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowFavoritesUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowWatchlistUseCase
import tv.trakt.trakt.core.user.data.local.favorites.UserFavoritesLocalDataSource
import tv.trakt.trakt.core.user.data.local.watchlist.UserWatchlistLocalDataSource
import tv.trakt.trakt.core.user.data.local.watchlist.WatchlistUpdates
import tv.trakt.trakt.core.user.data.local.watchlist.WatchlistUpdates.Source.Default
import tv.trakt.trakt.core.user.data.local.watchlist.minimal.UserWatchlistMinimalLocalDataSource
import tv.trakt.trakt.core.user.usecases.lists.LoadUserFavoritesUseCase
import tv.trakt.trakt.core.user.usecases.lists.LoadUserListsUseCase
import tv.trakt.trakt.core.user.usecases.lists.LoadUserWatchlistUseCase
import tv.trakt.trakt.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.core.user.usecases.ratings.LoadUserRatingsUseCase
import tv.trakt.trakt.resources.R
import tv.trakt.trakt.ui.components.dateselection.DateSelectionResult
import java.util.Locale

@OptIn(FlowPreview::class)
internal class ShowDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val appContext: Context,
    private val getDetailsUseCase: GetShowDetailsUseCase,
    private val getExternalRatingsUseCase: GetShowRatingsUseCase,
    private val getShowCreatorUseCase: GetShowCreatorUseCase,
    private val getShowTranslationsUseCase: GetShowTranslationsUseCase,
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
    private val appReviewUseCase: RequestAppReviewUseCase,
    private val userWatchlistLocalSource: UserWatchlistLocalDataSource,
    private val userWatchlistMinLocalSource: UserWatchlistMinimalLocalDataSource,
    private val userFavoritesLocalSource: UserFavoritesLocalDataSource,
    private val episodeLocalDataSource: EpisodeLocalDataSource,
    private val showDetailsUpdates: ShowDetailsUpdates,
    private val favoritesUpdates: FavoritesUpdates,
    private val watchlistUpdates: WatchlistUpdates,
    private val sessionManager: SessionManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<ShowDetailsDestination>()
    private val showId = destination.showId.toTraktId()

    private val initialState = ShowDetailsState()

    private val showState = MutableStateFlow(initialState.show)
    private val showRatingsState = MutableStateFlow(initialState.showRatings)
    private val showUserRatingsState = MutableStateFlow(initialState.showUserRating)
    private val showCreatorState = MutableStateFlow(initialState.showCreator)
    private val showProgressState = MutableStateFlow(initialState.showProgress)
    private val showTranslationState = MutableStateFlow(initialState.showTranslation)
    private val navigateEpisode = MutableStateFlow(initialState.navigateEpisode)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingProgress = MutableStateFlow(initialState.loadingProgress)
    private val loadingLists = MutableStateFlow(initialState.loadingLists)
    private val loadingFavorite = MutableStateFlow(initialState.loadingFavorite)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)
    private val userState = MutableStateFlow(initialState.user)

    private var ratingJob: Job? = null

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
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                var show = getDetailsUseCase.getLocalShow(showId)
                if (show == null) {
                    loadingState.update { Loading }
                    show = getDetailsUseCase.getShow(
                        showId = showId,
                    )
                }
                showState.update { show }

                loadTranslations()
                loadRatings(show)
                loadCreator()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
            }
        }
    }

    private fun loadProgressData(ignoreErrors: Boolean = false) {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingProgress.update { Loading }
                loadingLists.update { Loading }

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
                                it.showId == showId
                            }
                    }

                    val watchlistAsync = async { loadWatchlistUseCase.loadLocalShows() }
                    val listsAsync = async { loadListsUseCase.loadLocalLists() }

                    val progress = progressAsync.await()
                    val watchlist = watchlistAsync.await()
                    val lists = listsAsync.await()

                    showProgressState.update {
                        ShowDetailsState.ProgressState(
                            aired = showState.value?.airedEpisodes ?: 0,
                            plays = progress?.plays,
                            inWatchlist = watchlist.contains(showId),
                            inLists = lists.isNotEmpty(),
                        )
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.recordError(error)
                }
            } finally {
                loadingProgress.update { Done }
                loadingLists.update { Done }
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
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun loadCreator() {
        viewModelScope.launch {
            try {
                showCreatorState.update {
                    getShowCreatorUseCase.getCreator(showId)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun loadTranslations() {
        viewModelScope.launch {
            try {
                val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
                showTranslationState.update {
                    getShowTranslationsUseCase.getShowTranslations(
                        showId = showId,
                        locale = locale,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
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
                        loading = Loading,
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
                val userFavorite = loadFavoritesUseCase.loadLocalShows()
                    .associateBy { it.show.ids.trakt }[showId] != null

                val userRating = userRatings[showId]?.copy(
                    favorite = userFavorite,
                ) ?: UserRating(
                    mediaId = showId,
                    mediaType = SHOW,
                    rating = 0,
                    favorite = userFavorite,
                )

                showUserRatingsState.update {
                    UserRatingsState(
                        rating = userRating,
                        loading = Done,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    // History

    fun addToWatched(customDate: DateSelectionResult? = null) {
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
                loadingProgress.update { Loading }

                updateShowHistoryUseCase.addToWatched(
                    showId = showId,
                    customDate = customDate,
                )
                val progress = loadProgressUseCase.loadShowsProgress()
                    .firstOrNull {
                        it.showId == showId
                    }

                userWatchlistLocalSource.removeShows(ids = setOf(showId))
                userWatchlistMinLocalSource.removeShows(ids = setOf(showId))

                watchlistUpdates.notifyUpdate(Default)

                showProgressState.update {
                    it?.copy(
                        aired = showState.value?.airedEpisodes ?: 0,
                        plays = progress?.plays,
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
                    date = customDate?.analyticsStrings,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingProgress.update { Done }
            }
        }
    }

    fun removeFromWatched() {
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
                loadingProgress.update { Loading }

                updateShowHistoryUseCase.removeAllFromHistory(showId)
                loadProgressUseCase.loadShowsProgress()
                    .firstOrNull {
                        it.showId == showId
                    }

                showProgressState.update {
                    it?.copy(plays = 0)
                }

                showDetailsUpdates.notifyUpdate(Source.PROGRESS)
                infoState.update {
                    DynamicStringResource(R.string.text_info_history_removed)
                }
                analytics.progress.logRemoveWatchedMedia(
                    mediaType = "show",
                    source = "show_details",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingProgress.update { Done }
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
                loadingProgress.update { Loading }

                updateEpisodeHistoryUseCase.removePlayFromHistory(playId)
                val progress = loadProgressUseCase.loadShowsProgress()
                    .firstOrNull { it.showId == showId }

                showProgressState.update {
                    it?.copy(
                        aired = showState.value?.airedEpisodes ?: 0,
                        plays = progress?.plays,
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
                    Timber.recordError(error)
                }
            } finally {
                loadingProgress.update { Done }
            }
        }
    }

    // Lists

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
        ownerId: TraktId,
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
            add -> addToList(listId, ownerId)
            else -> removeFromList(listId, ownerId)
        }
    }

    private fun addToWatchlist() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingLists.update { Loading }

                updateShowWatchlistUseCase.addToWatchlist(showId)
                userWatchlistLocalSource.addShows(
                    shows = listOf(
                        WatchlistItem.ShowItem(
                            rank = 0,
                            show = showState.value!!,
                            listedAt = nowUtcInstant(),
                        ),
                    ),
                )
                userWatchlistMinLocalSource.addShows(
                    shows = setOf(showId),
                )

                watchlistUpdates.notifyUpdate(Default)

                showProgressState.update {
                    it?.copy(
                        inWatchlist = true,
                    )
                }

                infoState.update {
                    DynamicStringResource(R.string.text_info_watchlist_added)
                }
                appReviewUseCase.incrementCount()
                analytics.progress.logAddWatchlistMedia(
                    mediaType = "show",
                    source = "show_details",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingLists.update { Done }
            }
        }
    }

    private fun removeFromWatchlist() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingLists.update { Loading }

                updateShowWatchlistUseCase.removeFromWatchlist(showId)

                userWatchlistLocalSource.removeShows(ids = setOf(showId))
                userWatchlistMinLocalSource.removeShows(ids = setOf(showId))
                watchlistUpdates.notifyUpdate(Default)

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
                    Timber.recordError(error)
                }
            } finally {
                loadingLists.update { Done }
            }
        }
    }

    private fun addToList(
        listId: TraktId,
        ownerId: TraktId,
    ) {
        viewModelScope.launch {
            val show = showState.value
            if (!sessionManager.isAuthenticated() || show == null) {
                return@launch
            }
            try {
                loadingLists.update { Loading }

                addListItemUseCase.addShow(
                    listId = listId,
                    ownerId = ownerId,
                    show = show,
                )

                infoState.update {
                    DynamicStringResource(R.string.text_info_list_added)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingLists.update { Done }
            }
        }
    }

    private fun removeFromList(
        listId: TraktId,
        ownerId: TraktId,
    ) {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingLists.update { Loading }

                removeListItemUseCase.removeShow(
                    listId = listId,
                    ownerId = ownerId,
                    showId = showId,
                )

                refreshLists()
                infoState.update {
                    DynamicStringResource(R.string.text_info_list_removed)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingLists.update { Done }
            }
        }
    }

    private suspend fun refreshLists() {
        return coroutineScope {
            val watchlistAsync = async { loadWatchlistUseCase.loadLocalMovies() }
            val listsAsync = async { loadListsUseCase.loadLocalLists() }

            val watchlist = watchlistAsync.await()
            val lists = listsAsync.await()

            showProgressState.update {
                it?.copy(
                    inWatchlist = watchlist.contains(showId),
                    inLists = lists.isNotEmpty(),
                )
            }
        }
    }

    // Favorites

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
                loadingFavorite.update { Loading }

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
                    ) ?: UserRatingsState(
                        rating = UserRating(
                            mediaId = showId,
                            mediaType = SHOW,
                            rating = 0,
                            favorite = true,
                        ),
                    )
                }

                analytics.ratings.logFavoriteAdd(mediaType = "show")
                infoState.update {
                    DynamicStringResource(R.string.text_info_favorites_added)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingFavorite.update { Done }
            }
        }
    }

    private fun removeFromFavorites() {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                loadingFavorite.update { Loading }

                delay(300) // Small delay to allow UI to settle.
                updateShowFavoritesUseCase.removeFromFavorites(showId)
                userFavoritesLocalSource.removeShows(setOf(showId))
                favoritesUpdates.notifyUpdate(DETAILS)

                showUserRatingsState.update {
                    it?.copy(
                        rating = it.rating?.copy(favorite = false),
                    ) ?: UserRatingsState(
                        rating = UserRating(
                            mediaId = showId,
                            mediaType = SHOW,
                            rating = 0,
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
                    Timber.recordError(error)
                }
            } finally {
                loadingFavorite.update { Done }
            }
        }
    }

    // Ratings

    fun addRating(newRating: Int) {
        ratingJob?.cancel()
        ratingJob = viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }

            val currentRating = showUserRatingsState.value?.rating?.rating
            if (currentRating == newRating) {
                Timber.d("Rating is already $newRating, skipping update")
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
                    loading = Done,
                )
            }

            PostRatingWorker.scheduleOneTime(
                appContext = appContext,
                mediaId = showId,
                mediaType = SHOW,
                rating = newRating,
            )
        }
    }

    fun removeRating() {
        ratingJob?.cancel()
        ratingJob = viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }

            if (showUserRatingsState.value?.rating?.rating == 0) {
                Timber.d("Rating is already 0, skipping removal")
                return@launch
            }

            showUserRatingsState.update {
                UserRatingsState(
                    rating = UserRating(
                        mediaId = showId,
                        mediaType = SHOW,
                        rating = 0,
                        favorite = it?.rating?.favorite == true,
                    ),
                    loading = Done,
                )
            }

            PostRatingWorker.scheduleOneTime(
                appContext = appContext,
                mediaId = showId,
                mediaType = SHOW,
                rating = 0, // A rating of 0 indicates removal of rating
            )
        }
    }

    // Misc

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

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        showState,
        showRatingsState,
        showUserRatingsState,
        showCreatorState,
        showProgressState,
        showTranslationState,
        navigateEpisode,
        loadingState,
        loadingProgress,
        loadingLists,
        loadingFavorite,
        infoState,
        errorState,
        userState,
    ) { state ->
        ShowDetailsState(
            show = state[0] as Show?,
            showRatings = state[1] as ExternalRating?,
            showUserRating = state[2] as UserRatingsState?,
            showCreator = state[3] as Person?,
            showProgress = state[4] as ShowDetailsState.ProgressState?,
            showTranslation = state[5] as MediaTranslation?,
            navigateEpisode = state[6] as Pair<TraktId, Episode>?,
            loading = state[7] as LoadingState,
            loadingProgress = state[8] as LoadingState,
            loadingLists = state[9] as LoadingState,
            loadingFavorite = state[10] as LoadingState,
            info = state[11] as StringResource?,
            error = state[12] as Exception?,
            user = state[13] as User?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

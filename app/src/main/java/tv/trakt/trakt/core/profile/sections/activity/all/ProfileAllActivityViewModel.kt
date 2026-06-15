@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.profile.sections.activity.all

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.core.comments.usecases.GetCommentReactionsUseCase
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.core.comments.data.CommentsUpdates
import tv.trakt.trakt.core.comments.data.CommentsUpdates.Source.ALL_COMMENTS
import tv.trakt.trakt.core.comments.data.CommentsUpdates.Source.COMMENT_DETAILS
import tv.trakt.trakt.core.lists.ListsConfig.ACTIVITY_PAGE_LIMIT
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileActivityFilter
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileActivityFilter.Comments
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileActivityFilter.Ratings
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileCommentItem
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileRatingItem
import tv.trakt.trakt.core.profile.sections.activity.usecases.GetProfileCommentsUseCase
import tv.trakt.trakt.core.profile.sections.activity.usecases.GetProfileRatingsUseCase
import tv.trakt.trakt.core.profile.sections.activity.usecases.filters.GetActivityFilterUseCase
import tv.trakt.trakt.core.ratings.data.RatingsUpdates
import tv.trakt.trakt.core.ratings.data.RatingsUpdates.Source.POST_RATING
import kotlin.time.Duration.Companion.milliseconds

internal class ProfileAllActivityViewModel(
    private val getFilterUseCase: GetActivityFilterUseCase,
    private val getRatingsUseCase: GetProfileRatingsUseCase,
    private val getCommentsUseCase: GetProfileCommentsUseCase,
    private val getCommentReactionsUseCase: GetCommentReactionsUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val episodeLocalDataSource: EpisodeLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val ratingsUpdates: RatingsUpdates,
    private val commentsUpdates: CommentsUpdates,
) : ViewModel() {
    private val initialState = ProfileAllActivityState()

    private val userState = MutableStateFlow(initialState.user)
    private val ratingItemsState = MutableStateFlow(initialState.ratingItems)
    private val commentItemsState = MutableStateFlow(initialState.commentItems)
    private val reactionsState = MutableStateFlow(initialState.reactions)
    private val filterState = MutableStateFlow(initialState.filter)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingMoreState = MutableStateFlow(initialState.loadingMore)
    private val errorState = MutableStateFlow(initialState.error)

    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateEpisode = MutableStateFlow(initialState.navigateEpisode)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)

    private var loadDataJob: Job? = null
    private var processingJob: Job? = null

    private var page: Int = 1
    private var hasMoreData: Boolean = false

    init {
        loadData()
        observeRatings()
        observeComments()
    }

    @OptIn(FlowPreview::class)
    private fun observeRatings() {
        ratingsUpdates.observeUpdates(POST_RATING)
            .distinctUntilChanged()
            .debounce(200.milliseconds)
            .onEach {
                loadData(ignoreErrors = true)
            }
            .launchIn(viewModelScope)
    }

    @OptIn(FlowPreview::class)
    private fun observeComments() {
        merge(
            commentsUpdates.observeUpdates(ALL_COMMENTS),
            commentsUpdates.observeUpdates(COMMENT_DETAILS),
        )
            .distinctUntilChanged()
            .debounce(200.milliseconds)
            .onEach {
                loadData(ignoreErrors = true)
            }
            .launchIn(viewModelScope)
    }

    fun loadData(ignoreErrors: Boolean = false) {
        loadDataJob?.cancel()
        loadDataJob = viewModelScope.launch {
            try {
                page = 1

                loadingState.update { Idle }
                errorState.update { null }

                if (filterState.value == null) {
                    filterState.update {
                        getFilterUseCase.getFilter()
                    }
                }

                val filter = filterState.value ?: Ratings
                fetchLocalData(filter)

                loadingState.update {
                    when {
                        isCurrentItemsEmpty(filter) -> Loading
                        else -> Done
                    }
                }

                val itemCount = when (filter) {
                    Ratings -> {
                        val items = getRatingsUseCase.getRemoteRatings(
                            Pagination(page, ACTIVITY_PAGE_LIMIT),
                        )
                        ratingItemsState.update { items }
                        items.size
                    }
                    Comments -> {
                        val items = getCommentsUseCase.getRemoteComments(
                            Pagination(page, ACTIVITY_PAGE_LIMIT),
                        )
                        commentItemsState.update { items }
                        items.size
                    }
                }

                hasMoreData = itemCount >= ACTIVITY_PAGE_LIMIT
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
                loadDataJob = null
            }
        }
    }

    fun loadMoreData() {
        val filter = filterState.value ?: return
        if (isCurrentItemsEmpty(filter) || !hasMoreData) return
        if (loadingMoreState.value.isLoading || loadingState.value.isLoading) return

        viewModelScope.launch {
            try {
                loadingMoreState.update { Loading }

                val nextPage = page + 1
                val newData = when (filter) {
                    Ratings -> {
                        val newItems = getRatingsUseCase.getRemoteRatings(
                            Pagination(page, ACTIVITY_PAGE_LIMIT),
                        )
                        ratingItemsState.update { current ->
                            current
                                ?.plus(newItems)
                                ?.distinctBy { it.key }
                                ?.toImmutableList()
                        }
                        newItems.size
                    }
                    Comments -> {
                        val newItems = getCommentsUseCase.getRemoteComments(
                            Pagination(page, ACTIVITY_PAGE_LIMIT),
                        )
                        commentItemsState.update { current ->
                            current
                                ?.plus(newItems)
                                ?.distinctBy { it.key }
                                ?.toImmutableList()
                        }
                        newItems.size
                    }
                }

                page = nextPage
                hasMoreData = newData >= ACTIVITY_PAGE_LIMIT
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingMoreState.update { Done }
            }
        }
    }

    private suspend fun fetchLocalData(filter: ProfileActivityFilter) {
        when (filter) {
            Ratings -> ratingItemsState.update {
                getRatingsUseCase.getLocalRatings(ACTIVITY_PAGE_LIMIT)
            }
            Comments -> commentItemsState.update {
                getCommentsUseCase.getLocalComments(ACTIVITY_PAGE_LIMIT)
            }
        }
    }

    private fun isCurrentItemsEmpty(filter: ProfileActivityFilter): Boolean {
        return when (filter) {
            Ratings -> ratingItemsState.value.isNullOrEmpty()
            Comments -> commentItemsState.value.isNullOrEmpty()
        }
    }

    fun loadReactions(commentId: Int) {
        viewModelScope.launch {
            if (reactionsState.value?.containsKey(commentId) == true) {
                return@launch
            }

            try {
                val reactions = getCommentReactionsUseCase.getReactions(commentId)
                reactionsState.update { current ->
                    val mutable = current?.toMutableMap() ?: mutableMapOf()
                    mutable[commentId] = reactions
                    mutable.toImmutableMap()
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun setFilter(newFilter: ProfileActivityFilter) {
        if (newFilter == filterState.value || loadingState.value.isLoading) {
            return
        }
        viewModelScope.launch {
            getFilterUseCase.setFilter(newFilter)
            filterState.value = newFilter
            loadData()
        }
    }

    fun navigateToShow(show: Show) {
        if (navigateShow.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            showLocalDataSource.upsertShows(listOf(show))
            navigateShow.update { show.ids.trakt }
        }
    }

    fun navigateToEpisode(
        show: Show,
        episode: Episode,
    ) {
        if (navigateEpisode.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            showLocalDataSource.upsertShows(listOf(show))
            episodeLocalDataSource.upsertEpisodes(listOf(episode))

            navigateEpisode.update {
                Pair(show.ids.trakt, episode)
            }
        }
    }

    fun navigateToMovie(movie: Movie) {
        if (navigateMovie.value != null || processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            try {
                movieLocalDataSource.upsertMovies(listOf(movie))
                navigateMovie.update { movie.ids.trakt }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                processingJob = null
            }
        }
    }

    fun clearNavigation() {
        navigateShow.update { null }
        navigateEpisode.update { null }
        navigateMovie.update { null }
    }

    override fun onCleared() {
        loadDataJob?.cancel()
        loadDataJob = null
        super.onCleared()
    }

    val state = combine(
        loadingState,
        loadingMoreState,
        ratingItemsState,
        commentItemsState,
        reactionsState,
        filterState,
        navigateShow,
        navigateEpisode,
        navigateMovie,
        userState,
        errorState,
    ) { states ->
        ProfileAllActivityState(
            loading = states[0] as LoadingState,
            loadingMore = states[1] as LoadingState,
            ratingItems = states[2] as? ImmutableList<ProfileRatingItem>,
            commentItems = states[3] as? ImmutableList<ProfileCommentItem>,
            reactions = states[4] as? ImmutableMap<Int, ReactionsSummary>,
            filter = states[5] as? ProfileActivityFilter,
            navigateShow = states[6] as? TraktId,
            navigateEpisode = states[7] as? Pair<TraktId, Episode>,
            navigateMovie = states[8] as? TraktId,
            user = states[9] as? User,
            error = states[10] as? Exception,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

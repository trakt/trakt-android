@file:OptIn(FlowPreview::class)

package tv.trakt.trakt.core.profile.sections.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
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
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.core.comments.data.CommentsUpdates
import tv.trakt.trakt.core.comments.data.CommentsUpdates.Source.ALL_COMMENTS
import tv.trakt.trakt.core.comments.data.CommentsUpdates.Source.COMMENT_DETAILS
import tv.trakt.trakt.core.lists.ListsConfig.ACTIVITY_SECTION_LIMIT
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
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey
import kotlin.time.Duration.Companion.milliseconds

internal class ProfileActivityViewModel(
    private val getFilterUseCase: GetActivityFilterUseCase,
    private val getRatingsUseCase: GetProfileRatingsUseCase,
    private val getCommentsUseCase: GetProfileCommentsUseCase,
    private val getCommentReactionsUseCase: GetCommentReactionsUseCase,
    private val collapsingManager: CollapsingManager,
    private val showLocalDataSource: ShowLocalDataSource,
    private val episodeLocalDataSource: EpisodeLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val ratingsUpdates: RatingsUpdates,
    private val commentsUpdates: CommentsUpdates,
) : ViewModel() {
    private val initialState = ProfileActivityState()

    private val ratingItemsState = MutableStateFlow(initialState.ratingItems)
    private val commentItemsState = MutableStateFlow(initialState.commentItems)
    private val reactionsState = MutableStateFlow(initialState.reactions)
    private val filterState = MutableStateFlow(initialState.filter)
    private val collapseState = MutableStateFlow(isCollapsed())
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private val navigateShow = MutableStateFlow(initialState.navigateShow)
    private val navigateEpisode = MutableStateFlow(initialState.navigateEpisode)
    private val navigateMovie = MutableStateFlow(initialState.navigateMovie)

    private var dataJob: Job? = null
    private var collapseJob: Job? = null
    private var processingJob: Job? = null

    init {
        loadData()
        observeRatings()
        observeComments()
    }

    private fun observeRatings() {
        ratingsUpdates.observeUpdates(POST_RATING)
            .distinctUntilChanged()
            .debounce(200.milliseconds)
            .onEach {
                loadData(ignoreErrors = true)
            }
            .launchIn(viewModelScope)
    }

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
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            try {
                loadingState.update { Idle }
                errorState.update { null }
                ratingItemsState.update { null }
                commentItemsState.update { null }

                when (loadFilter()) {
                    Ratings -> loadRatingsData()
                    Comments -> loadCommentsData()
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

    private suspend fun loadRatingsData() {
        ratingItemsState.update {
            getRatingsUseCase.getLocalRatings(ACTIVITY_SECTION_LIMIT)
        }

        loadingState.update {
            when {
                ratingItemsState.value.isNullOrEmpty() -> Loading
                else -> Done
            }
        }

        ratingItemsState.update {
            getRatingsUseCase.getRemoteRatings(Pagination(1, ACTIVITY_SECTION_LIMIT))
        }
    }

    private suspend fun loadCommentsData() {
        commentItemsState.update {
            getCommentsUseCase.getLocalComments(ACTIVITY_SECTION_LIMIT)
        }

        loadingState.update {
            when {
                commentItemsState.value.isNullOrEmpty() -> Loading
                else -> Done
            }
        }

        commentItemsState.update {
            getCommentsUseCase.getRemoteComments(Pagination(1, ACTIVITY_SECTION_LIMIT))
        }
    }

    private suspend fun loadFilter(): ProfileActivityFilter {
        val filter = getFilterUseCase.getFilter()
        filterState.update { filter }
        return filter
    }

    fun loadReactions(commentId: Int) {
        viewModelScope.launch {
            if (reactionsState.value?.containsKey(commentId) == true) {
                // Reactions already loaded for this comment.
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
            loadData()
        }
    }

    fun setCollapsed(collapsed: Boolean) {
        collapseState.update { collapsed }

        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            when {
                collapsed -> collapsingManager.collapse(CollapsingKey.PROFILE_ACTIVITY)
                else -> collapsingManager.expand(CollapsingKey.PROFILE_ACTIVITY)
            }
        }
    }

    private fun isCollapsed(): Boolean {
        return collapsingManager.isCollapsed(CollapsingKey.PROFILE_ACTIVITY)
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

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        loadingState,
        ratingItemsState,
        commentItemsState,
        reactionsState,
        navigateShow,
        navigateEpisode,
        navigateMovie,
        filterState,
        collapseState,
        errorState,
    ) { state ->
        ProfileActivityState(
            loading = state[0] as LoadingState,
            ratingItems = state[1] as ImmutableList<ProfileRatingItem>?,
            commentItems = state[2] as ImmutableList<ProfileCommentItem>?,
            reactions = state[3] as ImmutableMap<Int, ReactionsSummary>?,
            navigateShow = state[4] as TraktId?,
            navigateEpisode = state[5] as Pair<TraktId, Episode>?,
            navigateMovie = state[6] as TraktId?,
            filter = state[7] as ProfileActivityFilter,
            collapsed = state[8] as Boolean,
            error = state[9] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

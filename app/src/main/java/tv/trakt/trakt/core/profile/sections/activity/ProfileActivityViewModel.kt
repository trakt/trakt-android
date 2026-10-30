@file:OptIn(FlowPreview::class)

package tv.trakt.trakt.core.profile.sections.activity

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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.core.comments.usecases.GetCommentReactionsUseCase
import tv.trakt.trakt.common.core.sync.changes.ChangeOrigin
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.data.local.EpisodeLocalSource
import tv.trakt.trakt.common.data.local.MovieLocalSource
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Idle
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider
import tv.trakt.trakt.common.helpers.lifecycle.observeForegroundRefresh
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaMode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.common.model.profile.ProfileCommentItem
import tv.trakt.trakt.common.model.profile.ProfileRatingItem
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.core.filters.data.GlobalFilterManager
import tv.trakt.trakt.core.lists.ListsConfig.ACTIVITY_SECTION_LIMIT
import tv.trakt.trakt.core.profile.helpers.matches
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileActivityFilter
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileActivityFilter.Comments
import tv.trakt.trakt.core.profile.sections.activity.model.ProfileActivityFilter.Ratings
import tv.trakt.trakt.core.profile.sections.activity.usecases.GetProfileCommentsUseCase
import tv.trakt.trakt.core.profile.sections.activity.usecases.GetProfileRatingsUseCase
import tv.trakt.trakt.core.profile.sections.activity.usecases.RateProfileRatingUseCase
import tv.trakt.trakt.core.profile.sections.activity.usecases.filters.GetActivityFilterUseCase
import tv.trakt.trakt.core.profile.sections.activity.usecases.withItemRating
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey
import kotlin.time.Duration.Companion.milliseconds

// Ratings and comments mix shows and movies, so a filtered mode fetches more to fill the section.
private const val ACTIVITY_FILTERED_FETCH_LIMIT = 100

internal class ProfileActivityViewModel(
    private val getFilterUseCase: GetActivityFilterUseCase,
    private val getRatingsUseCase: GetProfileRatingsUseCase,
    private val rateRatingUseCase: RateProfileRatingUseCase,
    private val getCommentsUseCase: GetProfileCommentsUseCase,
    private val getCommentReactionsUseCase: GetCommentReactionsUseCase,
    private val collapsingManager: CollapsingManager,
    private val showLocalSource: ShowLocalSource,
    private val episodeLocalSource: EpisodeLocalSource,
    private val movieLocalSource: MovieLocalSource,
    private val dataChanges: DataChanges,
    private val filterManager: GlobalFilterManager,
    private val appLifecycleProvider: AppLifecycleProvider,
) : ViewModel() {
    private val initialState = ProfileActivityState()
    private val origin = ChangeOrigin.unique()

    private val modeState = MutableStateFlow(filterManager.getFilter().mode)
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
        observeMode()
        observeForeground()
    }

    private fun observeMode() {
        filterManager.observeFilter()
            .map { it.mode }
            .filter { it != modeState.value }
            .onEach { mode ->
                modeState.update { mode }
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private fun observeForeground() {
        appLifecycleProvider.observeForegroundRefresh()
            .onEach { loadData(ignoreErrors = true) }
            .launchIn(viewModelScope)
    }

    private fun observeRatings() {
        dataChanges.observe(Kind.Ratings)
            .debounce(200.milliseconds)
            .onEach {
                loadData(ignoreErrors = true)
            }
            .launchIn(viewModelScope)
    }

    private fun observeComments() {
        dataChanges.observe(Kind.Comments, exclude = origin)
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
        val mode = modeState.value
        val fetchLimit = mode.fetchLimit()

        ratingItemsState.update {
            getRatingsUseCase.getLocalRatings(fetchLimit)
                .filter { it.matches(mode) }
                .take(ACTIVITY_SECTION_LIMIT)
                .toImmutableList()
        }

        loadingState.update {
            when {
                ratingItemsState.value.isNullOrEmpty() -> Loading
                else -> Done
            }
        }

        ratingItemsState.update {
            getRatingsUseCase.getRemoteRatings(Pagination(1, fetchLimit))
                .filter { it.matches(mode) }
                .take(ACTIVITY_SECTION_LIMIT)
                .toImmutableList()
        }
    }

    private suspend fun loadCommentsData() {
        val mode = modeState.value
        val fetchLimit = mode.fetchLimit()

        commentItemsState.update {
            getCommentsUseCase.getLocalComments(fetchLimit)
                .filter { it.matches(mode) }
                .take(ACTIVITY_SECTION_LIMIT)
                .toImmutableList()
        }

        loadingState.update {
            when {
                commentItemsState.value.isNullOrEmpty() -> Loading
                else -> Done
            }
        }

        commentItemsState.update {
            getCommentsUseCase.getRemoteComments(Pagination(1, fetchLimit))
                .filter { it.matches(mode) }
                .take(ACTIVITY_SECTION_LIMIT)
                .toImmutableList()
        }
    }

    private fun MediaMode.fetchLimit(): Int {
        return when (this) {
            MediaMode.Media -> ACTIVITY_SECTION_LIMIT
            MediaMode.Shows, MediaMode.Movies -> ACTIVITY_FILTERED_FETCH_LIMIT
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

    fun updateComment(comment: Comment) {
        commentItemsState.update { items ->
            items?.map { item ->
                if (item.comment.id == comment.id) item.withComment(comment) else item
            }?.toImmutableList()
        }
        dataChanges.notify(Kind.Comments, origin = origin)
    }

    fun deleteComment(commentId: TraktId) {
        commentItemsState.update { items ->
            items?.filterNot { it.comment.id == commentId.value }?.toImmutableList()
        }
        dataChanges.notify(Kind.Comments, origin = origin)
    }

    fun rateItem(
        item: ProfileRatingItem,
        rating: Int?,
    ) {
        viewModelScope.launch {
            if (!rateRatingUseCase.rateItem(item, rating, origin)) {
                return@launch
            }
            ratingItemsState.update { items ->
                items?.withItemRating(item.key, rating)
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
            showLocalSource.upsertShows(listOf(show))
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
            showLocalSource.upsertShows(listOf(show))
            episodeLocalSource.upsertEpisodes(listOf(episode))

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
                movieLocalSource.upsertMovies(listOf(movie))
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

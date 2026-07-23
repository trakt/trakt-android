package tv.trakt.trakt.core.summary.shows.features.seasons.all

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
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
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.comments.usecases.GetCommentReactionsUseCase
import tv.trakt.trakt.common.core.comments.usecases.GetCommentRepliesUseCase
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableSet
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.isNotNull
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.DateSelectionResult
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.Season
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.ratings.UserRating
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.comments.model.CommentsFilter
import tv.trakt.trakt.core.ratings.data.RatingsUpdates
import tv.trakt.trakt.core.ratings.data.RatingsUpdates.Source.POST_RATING
import tv.trakt.trakt.core.ratings.data.work.PostRatingWorker
import tv.trakt.trakt.core.reactions.data.ReactionsUpdates
import tv.trakt.trakt.core.reactions.data.ReactionsUpdates.Source.ALL_COMMENTS
import tv.trakt.trakt.core.reactions.data.work.DeleteReactionWorker
import tv.trakt.trakt.core.reactions.data.work.PostReactionWorker
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates
import tv.trakt.trakt.core.summary.episodes.data.EpisodeDetailsUpdates.Source
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source.AllSeasons
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source.Progress
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source.Seasons
import tv.trakt.trakt.core.summary.shows.data.ShowDetailsUpdates.Source.WatchedUntil
import tv.trakt.trakt.core.summary.shows.features.seasons.all.navigation.AllShowSeasonsDestination
import tv.trakt.trakt.core.summary.shows.features.seasons.all.usecases.GetSeasonCommentsUseCase
import tv.trakt.trakt.core.summary.shows.features.seasons.all.usecases.GetSeasonPeopleUseCase
import tv.trakt.trakt.core.summary.shows.features.seasons.model.SeasonItem
import tv.trakt.trakt.core.summary.shows.features.seasons.model.SeasonsMode
import tv.trakt.trakt.core.summary.shows.features.seasons.model.SeasonsPeopleMode
import tv.trakt.trakt.core.summary.shows.features.seasons.model.ShowSeasons
import tv.trakt.trakt.core.summary.shows.features.seasons.model.ShowSeasons.Helpers.markWatchedEpisodes
import tv.trakt.trakt.core.summary.shows.features.seasons.model.ShowSeasons.Helpers.markWatchedSeasons
import tv.trakt.trakt.core.summary.shows.features.seasons.usecases.GetShowSeasonsUseCase
import tv.trakt.trakt.core.summary.shows.usecases.GetShowDetailsUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.user.usecases.ratings.LoadUserRatingsUseCase
import tv.trakt.trakt.core.user.usecases.reactions.LoadUserReactionsUseCase
import tv.trakt.trakt.resources.R
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(FlowPreview::class)
internal class AllShowSeasonsViewModel(
    savedStateHandle: SavedStateHandle,
    private val appContext: Context,
    private val getShowDetailsUseCase: GetShowDetailsUseCase,
    private val getSeasonsUseCase: GetShowSeasonsUseCase,
    private val getSeasonsPeopleUseCase: GetSeasonPeopleUseCase,
    private val getSeasonCommentsUseCase: GetSeasonCommentsUseCase,
    private val getCommentRepliesUseCase: GetCommentRepliesUseCase,
    private val getCommentReactionsUseCase: GetCommentReactionsUseCase,
    private val loadUserReactionsUseCase: LoadUserReactionsUseCase,
    private val loadUserRatingsUseCase: LoadUserRatingsUseCase,
    private val reactionsUpdates: ReactionsUpdates,
    private val ratingsUpdates: RatingsUpdates,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val updateEpisodeHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val episodeLocalDataSource: EpisodeLocalDataSource,
    private val showDetailsUpdates: ShowDetailsUpdates,
    private val episodeDetailsUpdates: EpisodeDetailsUpdates,
    private val sessionManager: SessionManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = AllShowSeasonsState()

    private val destination = savedStateHandle.toRoute<AllShowSeasonsDestination>()
    private val showId = destination.showId.toTraktId()

    private val backgroundState = MutableStateFlow(destination.backgroundUrl)
    private val userState = MutableStateFlow(initialState.user)
    private val showState = MutableStateFlow(initialState.show)
    private val modeState = MutableStateFlow(initialState.mode)
    private val peopleModeState = MutableStateFlow(initialState.peopleMode)
    private val commentsFilterState = MutableStateFlow(initialState.commentsMode)
    private val commentReactionsState = MutableStateFlow(initialState.commentReactions)
    private val userReactionsState = MutableStateFlow(initialState.userReactions)
    private val itemsState = MutableStateFlow(initialState.items)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val loadingEpisodeState = MutableStateFlow(initialState.loadingEpisode)
    private val loadingSeasonState = MutableStateFlow(initialState.loadingSeason)
    private val navigateEpisode = MutableStateFlow(initialState.navigateEpisode)
    private val infoState = MutableStateFlow(initialState.info)
    private val errorState = MutableStateFlow(initialState.error)
    private val seasonUserRatingState = MutableStateFlow(initialState.seasonUserRating)

    private var seasonPeopleJob: Job? = null
    private var seasonCommentsJob: Job? = null
    private var seasonRepliesJob: Job? = null
    private var reactionJob: Job? = null
    private var ratingJob: Job? = null

    init {
        loadData()
        observeData()
    }

    private fun observeData() {
        merge(
            showDetailsUpdates.observeUpdates(Progress),
            showDetailsUpdates.observeUpdates(WatchedUntil),
            episodeDetailsUpdates.observeUpdates(Source.PROGRESS),
            episodeDetailsUpdates.observeUpdates(Source.SEASON),
            episodeDetailsUpdates.observeUpdates(Source.HISTORY),
        )
            .distinctUntilChanged()
            .debounce(200.milliseconds)
            .onEach {
                loadData(ignoreErrors = true)
            }
            .launchIn(viewModelScope)

        reactionsUpdates.observeUpdates(ALL_COMMENTS)
            .distinctUntilChanged()
            .debounce(200.milliseconds)
            .onEach {
                updateSeasonCommentReactions(it.first)
            }
            .launchIn(viewModelScope)

        ratingsUpdates.observeUpdates(POST_RATING)
            .distinctUntilChanged()
            .debounce(200.milliseconds)
            .onEach {
                refreshSeasonUserRating()
            }
            .launchIn(viewModelScope)
    }

    private fun loadData(ignoreErrors: Boolean = false) {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }

                coroutineScope {
                    val userAsync = async { sessionManager.getProfile() }
                    val showAsync = async {
                        getShowDetailsUseCase.getLocalShow(showId)
                            ?: getShowDetailsUseCase.getShow(showId)
                    }

                    userState.update { userAsync.await() }
                    showState.update { showAsync.await() }
                }

                showState.update {
                    getShowDetailsUseCase.getLocalShow(showId)
                        ?: getShowDetailsUseCase.getShow(showId)
                }

                loadUserReactions()

                val watched = when {
                    userState.isNotNull() -> {
                        val isLoaded = loadUserProgressUseCase.isShowsLoaded()
                        when {
                            isLoaded -> loadUserProgressUseCase.loadLocalShows()
                            else -> loadUserProgressUseCase.loadShowsProgress()
                        }.firstOrNull {
                            it.showId == showId
                        }
                    }
                    else -> {
                        null
                    }
                }

                val seasons = getSeasonsUseCase.getAllSeasons(
                    showId = showId,
                    initialSeason = destination.initialSeason ?: 1,
                )

                itemsState.update {
                    seasons.copy(
                        seasons = markWatchedSeasons(
                            inputSeasons = seasons.seasons,
                            progress = watched?.seasons,
                        ),
                        selectedSeasonEpisodes = markWatchedEpisodes(
                            inputEpisodes = seasons.selectedSeasonEpisodes,
                            progress = watched?.seasons,
                            checkable = userState.value != null,
                        ),
                    )
                }

                itemsState.value.selectedSeason?.let {
                    loadSeasonUserRating(it)
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

    fun loadSeason(season: SeasonItem) {
        if (
            loadingEpisodeState.value.isLoading ||
            loadingSeasonState.value.isLoading ||
            itemsState.value.isSeasonLoading ||
            season.season.number == itemsState.value.selectedSeason?.number
        ) {
            return
        }

        viewModelScope.launch {
            try {
                itemsState.update {
                    it.copy(
                        isSeasonLoading = true,
                        selectedSeason = season.season,
                    )
                }

                val progress = when {
                    userState.isNotNull() -> when {
                        loadUserProgressUseCase.isShowsLoaded() -> loadUserProgressUseCase.loadLocalShows()
                        else -> loadUserProgressUseCase.loadShowsProgress()
                    }.firstOrNull { it.showId == showId }

                    else -> null
                }

                val episodes = getSeasonsUseCase.getSeasonEpisodes(
                    showId = showId,
                    season = season.season.number,
                )

                itemsState.update {
                    it.copy(
                        selectedSeason = season.season,
                        selectedSeasonEpisodes = markWatchedEpisodes(
                            inputEpisodes = episodes,
                            progress = progress?.seasons,
                            checkable = userState.isNotNull(),
                        ),
                        isSeasonLoading = false,
                    )
                }

                loadSeasonUserRating(season.season)
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                    itemsState.update {
                        it.copy(isSeasonLoading = false)
                    }
                }
            }
        }
    }

    fun loadSeasonPeople(
        season: Season,
        clear: Boolean,
    ) {
        if (modeState.value != SeasonsMode.Info) {
            return
        }

        seasonPeopleJob?.cancel()
        seasonPeopleJob = viewModelScope.launch {
            try {
                itemsState.update {
                    if (clear) {
                        it.copy(
                            selectedSeasonCast = EmptyImmutableList,
                            selectedSeasonCrew = EmptyImmutableList,
                            isSeasonPeopleLoading = true,
                        )
                    } else {
                        it.copy(
                            isSeasonPeopleLoading = true,
                        )
                    }
                }

                val people = getSeasonsPeopleUseCase.getCastCrew(
                    showId = showId,
                    season = season.number,
                )

                itemsState.update {
                    it.copy(
                        selectedSeasonCast = people.cast,
                        selectedSeasonCrew = people.crew,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                seasonPeopleJob = null
                itemsState.update {
                    it.copy(
                        isSeasonPeopleLoading = false,
                    )
                }
            }
        }
    }

    fun loadSeasonComments(
        season: Season,
        clear: Boolean,
    ) {
        if (modeState.value != SeasonsMode.Reviews) {
            return
        }

        seasonCommentsJob?.cancel()
        seasonCommentsJob = viewModelScope.launch {
            try {
                itemsState.update {
                    if (clear) {
                        it.copy(
                            selectedSeasonComments = EmptyImmutableList,
                            selectedSeasonReplies = persistentMapOf(),
                            selectedSeasonRepliesLoading = EmptyImmutableSet,
                            isSeasonCommentsLoading = true,
                        )
                    } else {
                        it.copy(
                            isSeasonCommentsLoading = true,
                        )
                    }
                }

                val comments = getSeasonCommentsUseCase.getComments(
                    showId = showId,
                    season = season.number,
                    user = userState.value,
                    filter = commentsFilterState.value,
                )

                itemsState.update {
                    it.copy(
                        selectedSeasonComments = comments,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                seasonCommentsJob = null
                itemsState.update {
                    it.copy(
                        isSeasonCommentsLoading = false,
                    )
                }
            }
        }
    }

    fun loadSeasonCommentReplies(commentId: Int) {
        // Already loading replies.
        if (seasonRepliesJob?.isActive == true) {
            return
        }

        // Replies already loaded for this comment: collapse them.
        if (itemsState.value.selectedSeasonReplies.containsKey(commentId)) {
            itemsState.update {
                val mutable = it.selectedSeasonReplies.toMutableMap()
                mutable.remove(commentId)
                it.copy(selectedSeasonReplies = mutable.toImmutableMap())
            }
            return
        }

        seasonRepliesJob = viewModelScope.launch {
            try {
                itemsState.update {
                    val mutable = it.selectedSeasonRepliesLoading.toMutableSet()
                    mutable.add(commentId)
                    it.copy(selectedSeasonRepliesLoading = mutable.toImmutableSet())
                }

                val replies = getCommentRepliesUseCase.getCommentReplies(commentId)

                itemsState.update {
                    val mutable = it.selectedSeasonReplies.toMutableMap()
                    mutable[commentId] = replies
                    it.copy(selectedSeasonReplies = mutable.toImmutableMap())
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                seasonRepliesJob = null
                itemsState.update {
                    val mutable = it.selectedSeasonRepliesLoading.toMutableSet()
                    mutable.remove(commentId)
                    it.copy(selectedSeasonRepliesLoading = mutable.toImmutableSet())
                }
            }
        }
    }

    private suspend fun loadUserReactions() {
        if (!sessionManager.isAuthenticated()) {
            return
        }
        try {
            val userReactions = when {
                loadUserReactionsUseCase.isLoaded() -> loadUserReactionsUseCase.loadLocalReactions()
                else -> loadUserReactionsUseCase.loadReactions()
            }
            userReactionsState.update { userReactions }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }

    fun loadSeasonCommentReactions(commentId: Int) {
        if (commentReactionsState.value.containsKey(commentId)) {
            return
        }

        viewModelScope.launch {
            try {
                val reactions = getCommentReactionsUseCase.getReactions(commentId)
                commentReactionsState.update { current ->
                    val mutable = current.toMutableMap()
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

    fun setSeasonCommentReaction(
        reaction: Reaction,
        commentId: Int,
    ) {
        reactionJob?.cancel()
        reactionJob = viewModelScope.launch {
            try {
                val userReaction = userReactionsState.value[commentId]

                userReactionsState.update {
                    val mutable = it.toMutableMap()
                    mutable[commentId] = when {
                        userReaction == reaction -> null
                        else -> reaction
                    }
                    mutable.toImmutableMap()
                }

                commentReactionsState.update { current ->
                    val mutable = current.toMutableMap()
                    val summary = mutable[commentId] ?: ReactionsSummary(
                        reactionsCount = 0,
                        distribution = persistentMapOf(),
                    )
                    val updatedReactions = summary.copy(
                        reactionsCount = when (userReaction) {
                            reaction -> summary.reactionsCount - 1
                            null -> summary.reactionsCount + 1
                            else -> summary.reactionsCount
                        }.coerceAtLeast(0),
                        distribution = summary.distribution
                            .toMutableMap()
                            .apply {
                                // Add or increment the picked reaction (unless toggling it off).
                                if (userReaction != reaction) {
                                    this[reaction] = (this[reaction] ?: 0) + 1
                                }
                                // Decrement the previously selected reaction, if any.
                                userReaction?.let { previous ->
                                    this[previous] = ((this[previous] ?: 0) - 1).coerceAtLeast(0)
                                }
                            }
                            .filterValues { it > 0 }
                            .toImmutableMap(),
                    )
                    mutable[commentId] = updatedReactions
                    mutable.toImmutableMap()
                }

                // Debounce to avoid multiple rapid calls.
                delay(1.seconds)
                if (reaction == userReaction) {
                    deleteReactionWork(commentId = commentId)
                } else {
                    postReactionWork(commentId = commentId, reaction = reaction)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    private fun updateSeasonCommentReactions(commentId: Int) {
        viewModelScope.launch {
            try {
                if (!sessionManager.isAuthenticated()) {
                    return@launch
                }

                val reactions = getCommentReactionsUseCase.getReactions(commentId)
                val userReactions = when {
                    loadUserReactionsUseCase.isLoaded() -> loadUserReactionsUseCase.loadLocalReactions()
                    else -> loadUserReactionsUseCase.loadReactions()
                }

                userReactionsState.update { userReactions }
                commentReactionsState.update { current ->
                    val mutable = current.toMutableMap()
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

    private suspend fun postReactionWork(
        commentId: Int,
        reaction: Reaction,
    ) {
        if (!sessionManager.isAuthenticated()) {
            return
        }
        PostReactionWorker.scheduleOneTime(
            appContext = appContext,
            commentId = commentId,
            reaction = reaction,
            source = ALL_COMMENTS,
        )
    }

    private suspend fun deleteReactionWork(commentId: Int) {
        if (!sessionManager.isAuthenticated()) {
            return
        }
        DeleteReactionWorker.scheduleOneTime(
            appContext = appContext,
            commentId = commentId,
            source = ALL_COMMENTS,
        )
    }

    fun setMode(mode: SeasonsMode) {
        modeState.update { mode }
        itemsState.value.selectedSeason?.let {
            loadSeasonPeople(
                season = it,
                clear = false,
            )
            loadSeasonComments(
                season = it,
                clear = false,
            )
        }
    }

    fun addSeasonComment(comment: Comment) {
        itemsState.update {
            val mutable = it.selectedSeasonComments.toMutableList()
            mutable.add(0, comment)
            it.copy(selectedSeasonComments = mutable.toImmutableList())
        }
    }

    fun deleteSeasonComment(commentId: TraktId) {
        itemsState.update {
            it.copy(
                selectedSeasonComments = it.selectedSeasonComments
                    .filterNot { comment -> comment.id == commentId.value }
                    .toImmutableList(),
            )
        }
    }

    fun addSeasonReply(reply: Comment) {
        itemsState.update {
            val replies = it.selectedSeasonReplies.toMutableMap()
            val current = replies[reply.parentId]?.toMutableList() ?: mutableListOf()
            current.add(reply)
            replies[reply.parentId] = current.toImmutableList()

            val comments = it.selectedSeasonComments.map { comment ->
                if (comment.id == reply.parentId) {
                    comment.copy(replies = comment.replies + 1)
                } else {
                    comment
                }
            }.toImmutableList()

            it.copy(
                selectedSeasonReplies = replies.toImmutableMap(),
                selectedSeasonComments = comments,
            )
        }
    }

    fun deleteSeasonReply(
        parentId: TraktId,
        replyId: TraktId,
    ) {
        itemsState.update {
            val replies = it.selectedSeasonReplies.toMutableMap()
            replies[parentId.value]?.let { current ->
                replies[parentId.value] = current
                    .filterNot { reply -> reply.id == replyId.value }
                    .toImmutableList()
            }

            val comments = it.selectedSeasonComments.map { comment ->
                if (comment.id == parentId.value) {
                    comment.copy(replies = (comment.replies - 1).coerceAtLeast(0))
                } else {
                    comment
                }
            }.toImmutableList()

            it.copy(
                selectedSeasonReplies = replies.toImmutableMap(),
                selectedSeasonComments = comments,
            )
        }
    }

    fun setPeopleMode(peopleMode: SeasonsPeopleMode) {
        peopleModeState.update { peopleMode }
    }

    fun setCommentsFilter(filter: CommentsFilter) {
        if (commentsFilterState.value == filter) {
            return
        }

        commentsFilterState.update { filter }
        itemsState.value.selectedSeason?.let {
            loadSeasonComments(
                season = it,
                clear = true,
            )
        }
    }

    fun addToWatched(
        episode: Episode,
        customDate: DateSelectionResult? = null,
    ) {
        if (loadingState.value.isLoading ||
            loadingEpisodeState.value.isLoading ||
            loadingSeasonState.value.isLoading
        ) {
            return
        }

        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) return@launch

            try {
                loadingEpisodeState.update { Loading }
                setLoadingEpisode(episode)

                updateEpisodeHistoryUseCase.addToHistory(
                    episodeId = episode.ids.trakt,
                    customDate = customDate,
                )
                val progress = loadUserProgressUseCase.loadShowsProgress()
                    .firstOrNull { it.showId == showId }

                itemsState.update {
                    it.copy(
                        seasons = markWatchedSeasons(
                            inputSeasons = it.seasons,
                            progress = progress?.seasons,
                        ),
                        selectedSeasonEpisodes = markWatchedEpisodes(
                            inputEpisodes = itemsState.value.selectedSeasonEpisodes,
                            progress = progress?.seasons,
                            checkable = true,
                        ),
                    )
                }

                showDetailsUpdates.notifyUpdate(Seasons)
                showDetailsUpdates.notifyUpdate(AllSeasons)

                infoState.update { DynamicStringResource(R.string.text_info_history_added) }
                analytics.progress.logAddWatchedMedia(
                    mediaType = "episode",
                    source = "all_show_seasons_screen",
                    date = customDate?.analyticsStrings,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingEpisodeState.update { Done }
            }
        }
    }

    fun addToWatched(
        season: ShowSeasons,
        customDate: DateSelectionResult? = null,
    ) {
        if (loadingState.value.isLoading ||
            loadingEpisodeState.value.isLoading ||
            loadingSeasonState.value.isLoading
        ) {
            return
        }

        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) return@launch

            try {
                loadingSeasonState.update { Loading }

                val episodesToAdd = season.selectedSeasonEpisodes
                    .filter { !it.isWatched && it.episode.isReleased }
                    .map { it.episode.ids.trakt }

                updateEpisodeHistoryUseCase.addToHistory(
                    episodeIds = episodesToAdd,
                    customDate = customDate,
                )
                val progress = loadUserProgressUseCase.loadShowsProgress()
                    .firstOrNull { it.showId == showId }

                itemsState.update {
                    it.copy(
                        seasons = markWatchedSeasons(
                            inputSeasons = it.seasons,
                            progress = progress?.seasons,
                        ),
                        selectedSeasonEpisodes = markWatchedEpisodes(
                            inputEpisodes = itemsState.value.selectedSeasonEpisodes,
                            progress = progress?.seasons,
                            checkable = true,
                        ),
                    )
                }

                showDetailsUpdates.notifyUpdate(Seasons)
                showDetailsUpdates.notifyUpdate(AllSeasons)

                infoState.update { DynamicStringResource(R.string.text_info_history_added) }
                analytics.progress.logAddWatchedMedia(
                    mediaType = "season",
                    source = "all_show_seasons_screen",
                    date = customDate?.analyticsStrings,
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingSeasonState.update { Done }
            }
        }
    }

    fun removeFromWatched(episode: Episode) {
        if (loadingState.value.isLoading ||
            loadingEpisodeState.value.isLoading ||
            loadingSeasonState.value.isLoading
        ) {
            return
        }

        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) return@launch

            try {
                loadingEpisodeState.update { Loading }
                setLoadingEpisode(episode)

                updateEpisodeHistoryUseCase.removeEpisodeFromHistory(episode.ids.trakt.value)
                val progress = loadUserProgressUseCase.loadShowsProgress()
                    .firstOrNull { it.showId == showId }

                itemsState.update {
                    it.copy(
                        seasons = markWatchedSeasons(
                            inputSeasons = it.seasons,
                            progress = progress?.seasons,
                        ),
                        selectedSeasonEpisodes = markWatchedEpisodes(
                            inputEpisodes = itemsState.value.selectedSeasonEpisodes,
                            progress = progress?.seasons,
                            checkable = true,
                        ),
                    )
                }

                showDetailsUpdates.notifyUpdate(Seasons)
                showDetailsUpdates.notifyUpdate(AllSeasons)

                infoState.update { DynamicStringResource(R.string.text_info_history_removed) }
                analytics.progress.logRemoveWatchedMedia(
                    mediaType = "episode",
                    source = "all_show_seasons_screen",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingEpisodeState.update { Done }
            }
        }
    }

    fun removeFromWatched(season: Season) {
        if (loadingState.value.isLoading ||
            loadingEpisodeState.value.isLoading ||
            loadingSeasonState.value.isLoading
        ) {
            return
        }

        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) return@launch

            try {
                loadingSeasonState.update { Loading }

                updateEpisodeHistoryUseCase.removeSeasonFromHistory(season.ids.trakt.value)
                val progress = loadUserProgressUseCase.loadShowsProgress()
                    .firstOrNull { it.showId == showId }

                itemsState.update {
                    it.copy(
                        seasons = markWatchedSeasons(
                            inputSeasons = it.seasons,
                            progress = progress?.seasons,
                        ),
                        selectedSeasonEpisodes = markWatchedEpisodes(
                            inputEpisodes = itemsState.value.selectedSeasonEpisodes,
                            progress = progress?.seasons,
                            checkable = true,
                        ),
                    )
                }

                showDetailsUpdates.notifyUpdate(Seasons)
                showDetailsUpdates.notifyUpdate(AllSeasons)

                infoState.update { DynamicStringResource(R.string.text_info_history_removed) }
                analytics.progress.logRemoveWatchedMedia(
                    mediaType = "season",
                    source = "all_show_seasons_screen",
                )
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                }
            } finally {
                loadingSeasonState.update { Done }
            }
        }
    }

    fun navigateToEpisode(episode: Episode) {
        if (loadingState.value.isLoading ||
            loadingEpisodeState.value.isLoading ||
            loadingSeasonState.value.isLoading
        ) {
            return
        }

        viewModelScope.launch {
            episodeLocalDataSource.upsertEpisodes(listOf(episode))
            navigateEpisode.update {
                Pair(showId, episode)
            }
        }
    }

    private suspend fun setLoadingEpisode(episode: Episode) {
        itemsState.update {
            it.copy(
                selectedSeasonEpisodes = it.selectedSeasonEpisodes
                    .asyncMap { e ->
                        e.copy(isLoading = episode.ids.trakt == e.episode.ids.trakt)
                    }.toImmutableList(),
            )
        }
    }

    // Ratings

    private fun loadSeasonUserRating(season: Season) {
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                seasonUserRatingState.update { it.copy(loading = Loading) }

                if (!loadUserRatingsUseCase.isSeasonsLoaded()) {
                    loadUserRatingsUseCase.loadSeasons()
                }

                seasonUserRatingState.update {
                    AllShowSeasonsState.UserRatingState(
                        rating = loadUserRatingsUseCase.loadLocalSeasons()[season.ids.trakt],
                        loading = Done,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                    seasonUserRatingState.update { it.copy(loading = Done) }
                }
            }
        }
    }

    private fun refreshSeasonUserRating() {
        val season = itemsState.value.selectedSeason ?: return
        viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }
            try {
                val rating = loadUserRatingsUseCase.loadLocalSeasons()[season.ids.trakt]
                seasonUserRatingState.update {
                    it.copy(rating = rating, loading = Done)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun addSeasonRating(newRating: Int) {
        val season = itemsState.value.selectedSeason ?: return
        ratingJob?.cancel()
        ratingJob = viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }

            if (seasonUserRatingState.value.rating?.rating == newRating) {
                return@launch
            }

            seasonUserRatingState.update {
                AllShowSeasonsState.UserRatingState(
                    rating = UserRating(
                        mediaId = season.ids.trakt,
                        mediaType = MediaType.Season,
                        rating = newRating,
                    ),
                    loading = Done,
                )
            }

            PostRatingWorker.scheduleOneTime(
                appContext = appContext,
                mediaId = season.ids.trakt,
                mediaType = MediaType.Season,
                rating = newRating,
            )
        }
    }

    fun removeSeasonRating() {
        val season = itemsState.value.selectedSeason ?: return
        ratingJob?.cancel()
        ratingJob = viewModelScope.launch {
            if (!sessionManager.isAuthenticated()) {
                return@launch
            }

            if (seasonUserRatingState.value.rating?.rating == 0) {
                return@launch
            }

            seasonUserRatingState.update {
                AllShowSeasonsState.UserRatingState(
                    rating = UserRating(
                        mediaId = season.ids.trakt,
                        mediaType = MediaType.Season,
                        rating = 0,
                    ),
                    loading = Done,
                )
            }

            PostRatingWorker.scheduleOneTime(
                appContext = appContext,
                mediaId = season.ids.trakt,
                mediaType = MediaType.Season,
                rating = 0, // A rating of 0 indicates removal of rating
            )
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
        userState,
        modeState,
        peopleModeState,
        commentsFilterState,
        backgroundState,
        itemsState,
        loadingState,
        loadingEpisodeState,
        loadingSeasonState,
        navigateEpisode,
        infoState,
        errorState,
        commentReactionsState,
        userReactionsState,
        seasonUserRatingState,
    ) { state ->
        AllShowSeasonsState(
            show = state[0] as Show?,
            user = state[1] as User?,
            mode = state[2] as SeasonsMode,
            peopleMode = state[3] as SeasonsPeopleMode,
            commentsMode = state[4] as CommentsFilter,
            backgroundUrl = state[5] as String?,
            items = state[6] as ShowSeasons,
            loading = state[7] as LoadingState,
            loadingEpisode = state[8] as LoadingState,
            loadingSeason = state[9] as LoadingState,
            navigateEpisode = state[10] as Pair<TraktId, Episode>?,
            info = state[11] as StringResource?,
            error = state[12] as Exception?,
            commentReactions = state[13] as ImmutableMap<Int, ReactionsSummary>,
            userReactions = state[14] as ImmutableMap<Int, Reaction?>,
            seasonUserRating = state[15] as AllShowSeasonsState.UserRatingState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AllShowSeasonsState(backgroundUrl = destination.backgroundUrl),
    )
}

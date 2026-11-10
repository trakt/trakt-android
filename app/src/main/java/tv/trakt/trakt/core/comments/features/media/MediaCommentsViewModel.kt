package tv.trakt.trakt.core.comments.features.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.sync.changes.ChangeOrigin
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.reactions.Reaction
import tv.trakt.trakt.common.model.reactions.ReactionsSummary
import tv.trakt.trakt.core.comments.features.translation.data.CommentTranslationsStore
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslationEvent
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslationEvent.OpenExternalTranslation
import tv.trakt.trakt.core.comments.features.translation.model.CommentTranslations
import tv.trakt.trakt.core.comments.model.CommentsFilter
import tv.trakt.trakt.core.comments.model.CommentsMedia
import tv.trakt.trakt.core.comments.usecases.GetCommentsFilterUseCase
import tv.trakt.trakt.core.comments.usecases.GetCommentsLanguageUseCase
import tv.trakt.trakt.core.comments.usecases.GetCommentsUseCase
import tv.trakt.trakt.core.reactions.CommentReactionsDelegate
import tv.trakt.trakt.core.reactions.data.ReactionSource
import tv.trakt.trakt.helpers.collapsing.CollapseDelegate
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey
import kotlin.time.Duration.Companion.milliseconds

@OptIn(FlowPreview::class)
@Suppress("UNCHECKED_CAST")
internal class MediaCommentsViewModel(
    private val media: CommentsMedia,
    private val sessionManager: SessionManager,
    private val getFilterUseCase: GetCommentsFilterUseCase,
    private val getLanguageUseCase: GetCommentsLanguageUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val dataChanges: DataChanges,
    private val translationsStore: CommentTranslationsStore,
    private val analytics: Analytics,
    collapsingManager: CollapsingManager,
    reactionsFactory: CommentReactionsDelegate.Factory,
) : ViewModel() {
    private val initialState = MediaCommentsState(media = media)
    private val origin = ChangeOrigin.unique()

    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialState.filter)
    private val languageState = MutableStateFlow(initialState.language)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val userState = MutableStateFlow(initialState.user)
    private val errorState = MutableStateFlow(initialState.error)
    private val collapse = CollapseDelegate(
        manager = collapsingManager,
        scope = viewModelScope,
        key = media.collapsingKey,
    )
    private val reactions = reactionsFactory.create(
        scope = viewModelScope,
        source = media.reactionSource,
        origin = origin,
    )

    init {
        loadData()
        observeData()
    }

    private fun observeData() {
        dataChanges.observe(Kind.Reactions, exclude = origin)
            .debounce(200.milliseconds)
            .mapNotNull { it.targetId }
            .onEach(reactions::refresh)
            .launchIn(viewModelScope)

        dataChanges.observe(Kind.Comments, exclude = origin)
            .debounce(200.milliseconds)
            .onEach {
                loadData()
            }
            .launchIn(viewModelScope)
    }

    private suspend fun loadFilter(): CommentsFilter {
        val filter = getFilterUseCase.getFilter()
        filterState.update { filter }
        return filter
    }

    private suspend fun loadLanguage(): String? {
        val language = getLanguageUseCase.getLanguage()
        languageState.update { language }
        return language
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { Loading }

                loadUser()
                coroutineScope {
                    val commentsAsync = async { fetchComments() }
                    val userReactionsAsync = async { reactions.loadUserReactions() }

                    val comments = commentsAsync.await()
                    userReactionsAsync.await()

                    itemsState.update { comments }
                }
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

    private suspend fun loadUser() {
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

    private suspend fun fetchComments(): ImmutableList<Comment> {
        val user = userState.value
        val filter = loadFilter()
        val language = loadLanguage()

        return when (media) {
            is CommentsMedia.ForMovie -> getCommentsUseCase.getMovieComments(
                movieId = media.movie.ids.trakt,
                user = user,
                filter = filter,
                language = language,
            )

            is CommentsMedia.ForShow -> getCommentsUseCase.getShowComments(
                showId = media.show.ids.trakt,
                user = user,
                filter = filter,
                language = language,
            )

            is CommentsMedia.ForEpisode -> getCommentsUseCase.getEpisodeComments(
                showId = media.show.ids.trakt,
                seasonEpisode = media.episode.seasonEpisode,
                user = user,
                filter = filter,
                language = language,
            )
        }
    }

    fun loadReactions(commentId: Int) {
        reactions.load(commentId)
    }

    fun setFilter(filter: CommentsFilter) {
        if (loadingState.value != Done || filter == filterState.value) {
            return
        }

        viewModelScope.launch {
            getFilterUseCase.setFilter(filter)
            loadFilter()
            loadData()
        }
    }

    fun setLanguage(language: String?) {
        if (loadingState.value != Done || language == languageState.value) {
            return
        }

        viewModelScope.launch {
            getLanguageUseCase.setLanguage(language)
            loadLanguage()
            loadData()
        }
    }

    fun setReaction(
        reaction: Reaction,
        commentId: Int,
    ) {
        reactions.set(reaction, commentId)
    }

    fun logSpoilerReveal() {
        analytics.comments.logSpoilerReveal()
    }

    fun toggleTranslation(comment: Comment) {
        viewModelScope.launch {
            if (!translationsStore.toggle(comment)) {
                events.emit(OpenExternalTranslation(comment.comment.trim()))
            }
        }
    }

    fun addComment(comment: Comment) {
        itemsState.update {
            val mutable = it?.toMutableList() ?: mutableListOf()
            mutable.add(0, comment)
            mutable.toImmutableList()
        }
    }

    fun updateComment(comment: Comment) {
        itemsState.update {
            it?.map { existing ->
                if (existing.id == comment.id) comment else existing
            }?.toImmutableList()
        }
    }

    fun deleteComment(commentId: TraktId) {
        itemsState.update {
            it?.filterNot { comment -> comment.id == commentId.value }?.toImmutableList()
        }
    }

    fun setCollapsed(collapsed: Boolean) {
        collapse.set(collapsed)
    }

    val events: Flow<CommentTranslationEvent>
        field = MutableSharedFlow<CommentTranslationEvent>(replay = 0)

    val state = combine(
        itemsState,
        filterState,
        languageState,
        reactions.summaries,
        reactions.userReactions,
        loadingState,
        userState,
        errorState,
        collapse.state,
        translationsStore.translations,
    ) { state ->
        MediaCommentsState(
            media = media,
            items = state[0] as ImmutableList<Comment>?,
            filter = state[1] as CommentsFilter,
            language = state[2] as String?,
            reactions = state[3] as ImmutableMap<Int, ReactionsSummary>?,
            userReactions = state[4] as ImmutableMap<Int, Reaction?>?,
            loading = state[5] as LoadingState,
            user = state[6] as User?,
            error = state[7] as Exception?,
            collapsed = state[8] as Boolean,
            translations = state[9] as CommentTranslations,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

private val CommentsMedia.collapsingKey: CollapsingKey
    get() = when (this) {
        is CommentsMedia.ForMovie -> CollapsingKey.MOVIE_COMMENTS
        is CommentsMedia.ForShow -> CollapsingKey.SHOW_COMMENTS
        is CommentsMedia.ForEpisode -> CollapsingKey.EPISODE_COMMENTS
    }

private val CommentsMedia.reactionSource: ReactionSource
    get() = when (this) {
        is CommentsMedia.ForMovie -> ReactionSource.MovieComments
        is CommentsMedia.ForShow -> ReactionSource.ShowComments
        is CommentsMedia.ForEpisode -> ReactionSource.EpisodeComments
    }

package tv.trakt.trakt.core.comments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.DONE
import tv.trakt.trakt.common.helpers.LoadingState.LOADING
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.SeasonEpisode
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.toTraktId
import tv.trakt.trakt.core.comments.model.CommentsFilter
import tv.trakt.trakt.core.comments.navigation.CommentsDestination
import tv.trakt.trakt.core.summary.episodes.features.comments.usecases.GetEpisodeCommentsUseCase
import tv.trakt.trakt.core.summary.movies.features.comments.usecases.GetMovieCommentsUseCase
import tv.trakt.trakt.core.summary.shows.features.comments.usecases.GetShowCommentsUseCase

internal class CommentsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getShowCommentsUseCase: GetShowCommentsUseCase,
    private val getMovieCommentsUseCase: GetMovieCommentsUseCase,
    private val getEpisodeCommentsUseCase: GetEpisodeCommentsUseCase,
) : ViewModel() {
    private val initialState = CommentsState()

    private val backgroundState = MutableStateFlow(initialState.backgroundUrl)
    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialState.filter)
    private val userState = MutableStateFlow(initialState.user)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    private val destination = savedStateHandle.toRoute<CommentsDestination>()
    private val mediaId = destination.mediaId.toTraktId()
    private val mediaType = MediaType.valueOf(destination.mediaType)

    init {
        loadBackground()
        loadData()
    }

    private fun loadBackground() {
        backgroundState.update {
            destination.mediaImage
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                loadingState.update { LOADING }
                itemsState.update { fetchComments() }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.value = error
                    Timber.w(error)
                }
            } finally {
                loadingState.update { DONE }
            }
        }
    }

    private suspend fun fetchComments(): ImmutableList<Comment> {
        return when (mediaType) {
            MediaType.MOVIE -> getMovieCommentsUseCase.getComments(
                movieId = mediaId,
                filter = filterState.value,
                limit = 50,
            )
            MediaType.SHOW -> getShowCommentsUseCase.getComments(
                showId = mediaId,
                filter = filterState.value,
                limit = 50,
            )
            MediaType.EPISODE -> getEpisodeCommentsUseCase.getComments(
                showId = mediaId,
                seasonEpisode = SeasonEpisode(
                    season = destination.mediaSeason ?: -1,
                    episode = destination.mediaEpisode ?: -1,
                ),
                filter = filterState.value,
                limit = 50,
            )

            else -> throw IllegalArgumentException("Unsupported media type: $mediaType")
        }
    }

    fun setFilter(filter: CommentsFilter) {
        if (loadingState.value != DONE || filter == state.value.filter) {
            return
        }

        filterState.update { filter }
        loadData()
    }

    @Suppress("UNCHECKED_CAST")
    val state: StateFlow<CommentsState> = combine(
        backgroundState,
        itemsState,
        filterState,
        userState,
        loadingState,
        errorState,
    ) { state ->
        CommentsState(
            backgroundUrl = state[0] as String?,
            items = state[1] as ImmutableList<Comment>?,
            filter = state[2] as CommentsFilter,
            user = state[3] as User?,
            loading = state[4] as LoadingState,
            error = state[5] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

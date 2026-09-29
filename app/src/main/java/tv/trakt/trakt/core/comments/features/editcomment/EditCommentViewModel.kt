package tv.trakt.trakt.core.comments.features.editcomment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.CommentGif
import tv.trakt.trakt.core.comments.model.CommentMention
import tv.trakt.trakt.core.comments.model.MentionSource
import tv.trakt.trakt.core.comments.usecases.EditCommentUseCase
import tv.trakt.trakt.core.comments.usecases.GetCommentMentionsUseCase

@Suppress("UNCHECKED_CAST")
internal class EditCommentViewModel(
    private val comment: Comment,
    private val editCommentUseCase: EditCommentUseCase,
    private val mentionSource: MentionSource?,
    private val getCommentMentionsUseCase: GetCommentMentionsUseCase,
) : ViewModel() {
    private val initialState = EditCommentState()

    private val loadingState = MutableStateFlow(initialState.loading)
    private val resultState = MutableStateFlow(initialState.result)
    private val errorState = MutableStateFlow(initialState.error)
    private val mentionsState = MutableStateFlow(initialState.mentions)

    private var job: Job? = null

    init {
        loadMentions()
    }

    private fun loadMentions() {
        val source = mentionSource ?: return
        viewModelScope.launch {
            try {
                mentionsState.update {
                    getCommentMentionsUseCase.getMentions(source)
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    fun submitComment(
        text: String,
        spoiler: Boolean,
        gif: CommentGif?,
    ) {
        if (job?.isActive == true) {
            return
        }
        job = viewModelScope.launch {
            try {
                loadingState.update { Loading }
                resultState.update {
                    editCommentUseCase.editComment(
                        comment = comment,
                        text = text,
                        spoiler = spoiler,
                        gif = gif,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    loadingState.update { Done }
                    Timber.recordError(error)
                }
            } finally {
                job = null
            }
        }
    }

    fun clearError() {
        errorState.update { null }
    }

    override fun onCleared() {
        job?.cancel()
        job = null
        super.onCleared()
    }

    val state = combine(
        loadingState,
        resultState,
        errorState,
        mentionsState,
    ) { state ->
        EditCommentState(
            loading = state[0] as LoadingState,
            result = state[1] as Comment?,
            error = state[2] as Exception?,
            mentions = state[3] as ImmutableList<CommentMention>,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

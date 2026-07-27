package tv.trakt.trakt.core.comments.features.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.HTTP_ERROR_CONFLICT
import tv.trakt.trakt.common.helpers.extensions.HTTP_ERROR_RATE_LIMITED
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.core.comments.features.report.model.ReportReason
import tv.trakt.trakt.core.comments.usecases.ReportCommentUseCase
import tv.trakt.trakt.resources.R

internal class ReportCommentViewModel(
    private val comment: Comment,
    private val reportCommentUseCase: ReportCommentUseCase,
) : ViewModel() {
    private val initialState = ReportCommentState()

    private val loadingState = MutableStateFlow(initialState.loading)
    private val reportedState = MutableStateFlow(initialState.reported)
    private val errorState = MutableStateFlow(initialState.error)

    private var job: Job? = null

    fun report(
        reason: ReportReason,
        message: String,
    ) {
        if (job?.isActive == true) return

        loadingState.update { Loading }
        errorState.update { null }

        job = viewModelScope.launch {
            try {
                reportCommentUseCase.reportComment(
                    commentId = comment.id,
                    reason = reason.apiValue,
                    message = message,
                )
                reportedState.update { true }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    loadingState.update { Done }
                    errorState.update {
                        when (error.getHttpCode()) {
                            HTTP_ERROR_CONFLICT -> R.string.error_text_report_already_reported
                            HTTP_ERROR_RATE_LIMITED -> R.string.error_text_report_rate_limited
                            else -> R.string.error_text_report_unknown
                        }
                    }
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
        reportedState,
        errorState,
    ) { loading, reported, error ->
        ReportCommentState(
            loading = loading,
            reported = reported,
            error = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

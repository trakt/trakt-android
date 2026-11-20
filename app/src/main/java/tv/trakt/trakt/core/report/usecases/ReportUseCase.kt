package tv.trakt.trakt.core.report.usecases

import tv.trakt.trakt.common.data.remote.CommentsRemoteSource
import tv.trakt.trakt.common.data.remote.UserRemoteSource
import tv.trakt.trakt.core.report.model.ReportReason
import tv.trakt.trakt.core.report.model.ReportTarget

internal class ReportUseCase(
    private val commentsSource: CommentsRemoteSource,
    private val userSource: UserRemoteSource,
) {
    suspend fun report(
        target: ReportTarget,
        reason: ReportReason,
        message: String,
    ) {
        when (target) {
            is ReportTarget.Comment -> commentsSource.postReport(
                commentId = target.id,
                reason = reason.apiValue,
                message = message,
            )
            is ReportTarget.User -> userSource.postReport(
                userId = target.id,
                reason = reason.apiValue,
                message = message,
            )
        }
    }
}

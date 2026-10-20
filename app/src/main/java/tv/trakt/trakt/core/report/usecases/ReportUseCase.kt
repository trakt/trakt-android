package tv.trakt.trakt.core.report.usecases

import tv.trakt.trakt.common.data.remote.CommentsRemoteSource
import tv.trakt.trakt.common.data.remote.EpisodesRemoteSource
import tv.trakt.trakt.common.data.remote.MoviesRemoteSource
import tv.trakt.trakt.common.data.remote.ShowsRemoteSource
import tv.trakt.trakt.core.report.model.ReportReason
import tv.trakt.trakt.core.report.model.ReportTarget

internal class ReportUseCase(
    private val commentsSource: CommentsRemoteSource,
    private val moviesSource: MoviesRemoteSource,
    private val showsSource: ShowsRemoteSource,
    private val episodesSource: EpisodesRemoteSource,
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
            is ReportTarget.Movie -> moviesSource.postReport(
                movieId = target.id,
                reason = reason.apiValue,
                message = message,
            )
            is ReportTarget.Show -> showsSource.postReport(
                showId = target.id,
                reason = reason.apiValue,
                message = message,
            )
            is ReportTarget.Episode -> episodesSource.postReport(
                showId = target.showId,
                season = target.season,
                episode = target.episode,
                reason = reason.apiValue,
                message = message,
            )
        }
    }
}

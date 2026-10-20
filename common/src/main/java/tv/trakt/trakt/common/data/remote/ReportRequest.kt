package tv.trakt.trakt.common.data.remote

import org.openapitools.client.models.PostMoviesReportRequest

internal fun createReportRequest(
    reason: String,
    message: String,
): PostMoviesReportRequest {
    return PostMoviesReportRequest(
        reason = PostMoviesReportRequest.Reason.entries.first { it.value == reason },
        message = message.ifBlank { null },
    )
}

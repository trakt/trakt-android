package tv.trakt.trakt.core.applinks.usecases

import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.networking.api.v3.V3Api
import tv.trakt.trakt.common.networking.api.v3.model.V3ShareClickRequest
import tv.trakt.trakt.core.applinks.model.AppLink
import tv.trakt.trakt.core.applinks.model.AppLinkShare
import tv.trakt.trakt.core.applinks.model.ShareArrivalResult

/**
 * Credits the sharer when a shared link is opened and reports the result.
 * Never throws: failures are reported as [ShareArrivalResult.Failed].
 */
internal class RecordShareArrivalUseCase(
    private val sessionManager: SessionManager,
    private val v3Api: V3Api,
    private val analytics: Analytics,
) {
    suspend fun record(link: AppLink) {
        val share = link.share ?: return
        if (isOwnShare(share)) return

        analytics.logShareArrival(
            type = link.shareType(),
            result = resultOf(share).value,
        )
    }

    private suspend fun isOwnShare(share: AppLinkShare): Boolean {
        val ownCode = sessionManager.getProfile()?.settings?.shareCode ?: return false
        return share.code == ownCode
    }

    private suspend fun resultOf(share: AppLinkShare): ShareArrivalResult {
        if (!sessionManager.isAuthenticated()) return ShareArrivalResult.Anonymous

        return try {
            val response = v3Api.postShareClick(
                V3ShareClickRequest(
                    code = share.code,
                    url = share.url,
                ),
            )
            ShareArrivalResult.fromApi(response.result) ?: ShareArrivalResult.Failed
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.w(error, "Failed to record share click")
            }
            ShareArrivalResult.Failed
        }
    }
}

private fun AppLink.shareType(): String {
    return when (this) {
        is AppLink.Show -> "show"
        is AppLink.Movie -> "movie"
        is AppLink.Person -> "person"
        is AppLink.Imdb -> "other"
    }
}

package tv.trakt.trakt.core.notifications.push.usecases

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.data.local.PushTokenLocalSource
import tv.trakt.trakt.common.data.remote.UserRemoteSource

internal class SyncPushTokenUseCase(
    private val sessionManager: SessionManager,
    private val firebaseMessaging: FirebaseMessaging,
    private val remoteSource: UserRemoteSource,
    private val localSource: PushTokenLocalSource,
) {
    suspend fun syncToken() {
        if (!sessionManager.isAuthenticated()) return

        // register() targets the installation ID, which survives sign-out and would keep the
        // previous account's server row live. Stay on tokens until the server can delete rows.
        @Suppress("DEPRECATION")
        val token = firebaseMessaging.token.await()
        if (token == localSource.getUploadedToken()) {
            Timber.d("Push token already uploaded: $token")
            return
        }

        remoteSource.postPushToken(
            token = token,
            deviceId = localSource.getDeviceId(),
        )
        localSource.setUploadedToken(token)

        Timber.d("Push token uploaded")
    }
}

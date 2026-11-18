package tv.trakt.trakt.core.notifications.push.usecases

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

internal class DeletePushTokenUseCase(
    private val firebaseMessaging: FirebaseMessaging,
) {
    suspend fun deleteToken() {
        @Suppress("DEPRECATION")
        firebaseMessaging.deleteToken().await()
    }
}

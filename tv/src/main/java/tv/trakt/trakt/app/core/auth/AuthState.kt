package tv.trakt.trakt.app.core.auth

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.app.core.auth.model.AuthDeviceCode

@Immutable
internal data class AuthState(
    val loadingState: LoadingState? = null,
    val authDeviceCode: AuthDeviceCode? = null,
    val backgroundUrl: String? = null,
    val error: Exception? = null,
) {
    enum class LoadingState {
        LOADING,
        SUCCESS,
        REJECTED,
    }
}

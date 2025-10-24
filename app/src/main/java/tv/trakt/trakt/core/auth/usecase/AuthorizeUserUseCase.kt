package tv.trakt.trakt.core.auth.usecase

import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.delay
import timber.log.Timber
import tv.trakt.trakt.common.auth.TokenProvider
import tv.trakt.trakt.core.auth.data.remote.AuthRemoteDataSource

internal val authCodeKey = stringPreferencesKey("auth_code")

internal class AuthorizeUserUseCase(
    private val remoteSource: AuthRemoteDataSource,
    private val tokenProvider: TokenProvider,
) {
    suspend fun authorizeByCode(code: String) {
        val token = remoteSource.getAccessToken(code)
        tokenProvider.saveToken(token)
        delay(500) // Small delay to ensure token is stored before proceeding.
        Timber.d("Received and stored access token!")
    }
}

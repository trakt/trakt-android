package tv.trakt.trakt.common.networking.api.users

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import org.openapitools.client.infrastructure.ApiClient
import tv.trakt.trakt.common.data.request.UsernameSettingsRequest

class UsersExtrasApi(
    private val baseUrl: String,
    httpClientEngine: HttpClientEngine,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit),
) : ApiClient(
        baseUrl,
        httpClientEngine,
        httpClientConfig,
    ) {
    suspend fun putUsersSaveUsername(username: String) {
        client.put("${baseUrl}users/settings") {
            setBody(
                UsernameSettingsRequest(
                    user = UsernameSettingsRequest.User(username = username),
                ),
            )
        }
    }
}

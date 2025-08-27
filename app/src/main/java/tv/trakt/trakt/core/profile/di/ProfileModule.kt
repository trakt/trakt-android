package tv.trakt.trakt.core.profile.di

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.CalendarsApi
import org.openapitools.client.apis.HistoryApi
import org.openapitools.client.apis.UsersApi
import tv.trakt.trakt.app.Config.API_BASE_URL
import tv.trakt.trakt.core.auth.di.AUTH_PREFERENCES
import tv.trakt.trakt.core.profile.ProfileViewModel
import tv.trakt.trakt.core.profile.data.remote.UserApiClient
import tv.trakt.trakt.core.profile.data.remote.UserRemoteDataSource
import tv.trakt.trakt.core.profile.usecase.GetUserProfileUseCase
import tv.trakt.trakt.core.profile.usecase.LogoutUserUseCase

internal val profileDataModule = module {
    single<UserRemoteDataSource> {
        val httpClientEngine = get<HttpClientEngine>()
        val httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("authorizedClientConfig"))

        UserApiClient(
            usersApi = UsersApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = httpClientEngine,
                httpClientConfig = httpClientConfig,
            ),
            historyApi = HistoryApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = httpClientEngine,
                httpClientConfig = httpClientConfig,
            ),
            calendarsApi = CalendarsApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = httpClientEngine,
                httpClientConfig = httpClientConfig,
            ),
        )
    }
}

internal val profileModule = module {
    factory {
        GetUserProfileUseCase(
            sessionManager = get(),
            remoteSource = get(),
        )
    }

    factory {
        LogoutUserUseCase(
            sessionManager = get(),
        )
    }

    viewModel {
        ProfileViewModel(
            sessionManager = get(),
            authorizeUseCase = get(),
            authorizePreferences = get(named(AUTH_PREFERENCES)),
            getProfileUseCase = get(),
            logoutUseCase = get(),
        )
    }
}

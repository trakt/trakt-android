package tv.trakt.app.tv.core.auth.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.OauthApi
import tv.trakt.app.tv.Config.API_BASE_URL
import tv.trakt.app.tv.core.auth.AuthViewModel
import tv.trakt.app.tv.core.auth.data.remote.AuthApiClient
import tv.trakt.app.tv.core.auth.data.remote.AuthRemoteDataSource
import tv.trakt.app.tv.core.auth.usecases.GetDeviceCodeUseCase
import tv.trakt.app.tv.core.auth.usecases.GetDeviceTokenUseCase
import tv.trakt.app.tv.core.auth.usecases.LoadUserProfileUseCase

internal val authDataModule = module {
    single<AuthRemoteDataSource> {
        AuthApiClient(
            api = OauthApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = get(),
                httpClientConfig = get(named("clientConfig")),
            ),
        )
    }
}

internal val authModule = module {
    factory {
        GetDeviceCodeUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetDeviceTokenUseCase(
            remoteSource = get(),
            tokenProvider = get(),
        )
    }

    factory {
        LoadUserProfileUseCase(
            remoteSource = get(),
            sessionManager = get(),
        )
    }

    viewModel {
        AuthViewModel(
            getDeviceCodeUseCase = get(),
            getDeviceTokenUseCase = get(),
            loadUserProfileUseCase = get(),
            sessionManager = get(),
        )
    }
}

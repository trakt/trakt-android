package tv.trakt.trakt.core.billing.di

import io.ktor.client.HttpClientConfig
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.Config.WEB_BASE_URL
import tv.trakt.trakt.core.billing.BillingViewModel
import tv.trakt.trakt.core.billing.data.remote.BillingApiClient
import tv.trakt.trakt.core.billing.data.remote.BillingRemoteDataSource
import tv.trakt.trakt.core.billing.usecases.VerifyPurchaseUseCase

internal val billingDataModule = module {
    single<BillingRemoteDataSource> {
        BillingApiClient(
            baseUrl = WEB_BASE_URL,
            httpClientEngine = get(),
            httpClientConfig = get<(HttpClientConfig<*>) -> Unit>(named("clientConfig")),
        )
    }
}

internal val billingModule = module {
    factoryOf(::VerifyPurchaseUseCase)

    viewModel {
        BillingViewModel(
            appContext = androidApplication(),
            sessionManager = get(),
            analytics = get(),
            verifyPurchaseUseCase = get(),
            loadUserUseCase = get(),
        )
    }
}

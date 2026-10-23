package tv.trakt.trakt.core.billing.di

import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.core.billing.BillingViewModel
import tv.trakt.trakt.core.billing.usecases.GetVipUsageLimitsUseCase
import tv.trakt.trakt.core.billing.usecases.VerifyPurchaseUseCase

internal val billingModule = module {
    factoryOf(::VerifyPurchaseUseCase)
    factoryOf(::GetVipUsageLimitsUseCase)

    viewModel {
        BillingViewModel(
            savedStateHandle = get(),
            appContext = androidApplication(),
            sessionManager = get(),
            analytics = get(),
            verifyPurchaseUseCase = get(),
            loadUserUseCase = get(),
            getUsageLimitsUseCase = get(),
        )
    }
}

package tv.trakt.trakt.core.billing.di

import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.core.billing.BillingViewModel

internal val billingModule = module {

    viewModel {
        BillingViewModel(
            appContext = androidApplication(),
            sessionManager = get(),
            analytics = get(),
        )
    }
}

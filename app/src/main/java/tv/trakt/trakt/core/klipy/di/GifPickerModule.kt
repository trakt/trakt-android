package tv.trakt.trakt.core.klipy.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.core.klipy.GifPickerViewModel

val gifPickerModule = module {
    viewModel { (defaultQuery: String?) ->
        GifPickerViewModel(
            defaultQuery = defaultQuery,
            remoteSource = get(),
            sessionManager = get(),
        )
    }
}

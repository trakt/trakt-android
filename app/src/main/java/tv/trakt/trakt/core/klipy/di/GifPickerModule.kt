package tv.trakt.trakt.core.klipy.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.core.klipy.GifPickerSource
import tv.trakt.trakt.core.klipy.GifPickerViewModel

val gifPickerModule = module {
    viewModel { (defaultQuery: String?, source: GifPickerSource) ->
        GifPickerViewModel(
            defaultQuery = defaultQuery,
            source = source,
            remoteSource = get(),
            sessionManager = get(),
            analytics = get(),
        )
    }
}

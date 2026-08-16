package tv.trakt.trakt.core.klipy.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.core.klipy.GifPickerViewModel

val gifPickerModule = module {
    viewModelOf(::GifPickerViewModel)
}

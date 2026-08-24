package tv.trakt.trakt.ui.components.dateselection.otherdatepicker.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.ui.components.dateselection.otherdatepicker.OtherDatePickerViewModel

internal val otherDatePickerModule = module {
    viewModelOf(::OtherDatePickerViewModel)
}

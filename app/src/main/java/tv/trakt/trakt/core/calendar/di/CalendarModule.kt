package tv.trakt.trakt.core.calendar.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tv.trakt.trakt.core.calendar.CalendarViewModel
import tv.trakt.trakt.core.calendar.usecases.GetCalendarItemsUseCase

internal val calendarModule = module {
    factoryOf(::GetCalendarItemsUseCase)

    viewModelOf(::CalendarViewModel)
}

package tv.trakt.trakt.core.calendar.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.core.calendar.CalendarViewModel
import tv.trakt.trakt.core.calendar.usecases.GetCalendarItemsUseCase

internal val calendarModule = module {
    factory {
        GetCalendarItemsUseCase(
            remoteUserSource = get(),
        )
    }

    viewModel {
        CalendarViewModel(
            sessionManager = get(),
            getCalendarItemsUseCase = get(),
            showLocalDataSource = get(),
            episodeLocalDataSource = get(),
        )
    }
}

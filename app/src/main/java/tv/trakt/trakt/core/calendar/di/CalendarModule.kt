package tv.trakt.trakt.core.calendar.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.core.calendar.CalendarViewModel

internal val calendarModule = module {
    viewModel {
        CalendarViewModel(
            sessionManager = get(),
            remoteUserSource = get(),
            showLocalDataSource = get(),
            episodeLocalDataSource = get(),
        )
    }
}

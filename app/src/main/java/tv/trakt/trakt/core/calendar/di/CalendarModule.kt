package tv.trakt.trakt.core.calendar.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.core.calendar.CalendarViewModel
import tv.trakt.trakt.core.calendar.usecases.GetCalendarItemsUseCase

internal val calendarModule = module {
    factory {
        GetCalendarItemsUseCase(
            loadUserProgressUseCase = get(),
            remoteUserSource = get(),
            sessionManager = get(),
        )
    }

    viewModel {
        CalendarViewModel(
            getCalendarItemsUseCase = get(),
            updateEpisodeHistoryUseCase = get(),
            updateMovieHistoryUseCase = get(),
            loadUserProgressUseCase = get(),
            showLocalDataSource = get(),
            movieLocalDataSource = get(),
            episodeLocalDataSource = get(),
            showUpdates = get(),
            episodeUpdates = get(),
            movieUpdates = get(),
            sessionManager = get(),
            analytics = get(),
        )
    }
}

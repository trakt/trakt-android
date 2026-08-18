package tv.trakt.trakt.widgets.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import tv.trakt.trakt.widgets.configuration.WidgetConfigurationViewModel
import tv.trakt.trakt.widgets.data.WidgetAppearanceStore
import tv.trakt.trakt.widgets.data.WidgetsUpdater
import tv.trakt.trakt.widgets.widget.calendar.CalendarWidgetUpdater
import tv.trakt.trakt.widgets.widget.calendar.data.CalendarWidgetDataSource
import tv.trakt.trakt.widgets.widget.continuewatching.ContinueWatchingWidgetUpdater
import tv.trakt.trakt.widgets.widget.continuewatching.data.ContinueWatchingWidgetDataSource
import tv.trakt.trakt.widgets.widget.continuewatching.usecases.WidgetAddToHistoryUseCase
import tv.trakt.trakt.widgets.widget.streaks.StreaksWidgetUpdater
import tv.trakt.trakt.widgets.widget.streaks.data.StreaksWidgetDataSource

internal val widgetsModule = module {
    singleOf(::ContinueWatchingWidgetDataSource)
    singleOf(::CalendarWidgetDataSource)
    singleOf(::StreaksWidgetDataSource)
    factoryOf(::WidgetAddToHistoryUseCase)

    singleOf(::WidgetsUpdater)

    single {
        ContinueWatchingWidgetUpdater(
            context = androidApplication(),
            dataSource = get(),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        )
    }

    single {
        CalendarWidgetUpdater(
            context = androidApplication(),
            dataSource = get(),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        )
    }

    single {
        StreaksWidgetUpdater(
            context = androidApplication(),
            dataSource = get(),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        )
    }

    single {
        WidgetAppearanceStore(
            context = androidApplication(),
        )
    }

    viewModel { (appWidgetId: Int) ->
        WidgetConfigurationViewModel(
            appWidgetId = appWidgetId,
            appearanceStore = get(),
            continueWatchingUpdater = get(),
            calendarUpdater = get(),
            streaksUpdater = get(),
        )
    }
}

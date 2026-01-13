package tv.trakt.trakt.core.notifications.di

import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module
import tv.trakt.trakt.core.notifications.data.work.ScheduleNotificationsWorker

internal val notificationsModule = module {
    worker {
        ScheduleNotificationsWorker(
            appContext = androidApplication(),
            workerParams = get(),
            sessionManager = get(),
            getUpcomingUseCase = get(),
        )
    }
}

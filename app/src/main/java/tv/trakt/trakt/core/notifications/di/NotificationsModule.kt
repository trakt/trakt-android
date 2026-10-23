package tv.trakt.trakt.core.notifications.di

import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.workmanager.dsl.worker
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.dsl.module
import tv.trakt.trakt.core.notifications.data.work.PostNotificationWorker
import tv.trakt.trakt.core.notifications.data.work.ScheduleNotificationsWorker
import tv.trakt.trakt.core.notifications.data.work.mir.PostMonthReviewNotificationWorker
import tv.trakt.trakt.core.notifications.data.work.mir.ScheduleMonthReviewNotificationWorker

internal val notificationsModule = module {
    worker {
        ScheduleNotificationsWorker(
            appContext = androidApplication(),
            workerParams = get(),
            sessionManager = get(),
            getUpcomingUseCase = get(),
            enableNotificationsUseCase = get(),
            notificationsDeliveryUseCase = get(),
        )
    }

    worker {
        PostNotificationWorker(
            appContext = androidApplication(),
            workerParams = get(),
            enableNotificationsUseCase = get(),
        )
    }

    workerOf(::PostMonthReviewNotificationWorker)
    workerOf(::ScheduleMonthReviewNotificationWorker)
}

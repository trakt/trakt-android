package tv.trakt.trakt.core.notifications.di

import com.google.firebase.messaging.FirebaseMessaging
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.workmanager.dsl.worker
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import tv.trakt.trakt.core.notifications.data.work.PostNotificationWorker
import tv.trakt.trakt.core.notifications.data.work.ScheduleNotificationsWorker
import tv.trakt.trakt.core.notifications.data.work.mir.PostMonthReviewNotificationWorker
import tv.trakt.trakt.core.notifications.data.work.mir.ScheduleMonthReviewNotificationWorker
import tv.trakt.trakt.core.notifications.push.data.work.DeletePushTokenWorker
import tv.trakt.trakt.core.notifications.push.data.work.SyncPushTokenWorker
import tv.trakt.trakt.core.notifications.push.usecases.DeletePushTokenUseCase
import tv.trakt.trakt.core.notifications.push.usecases.SyncPushTokenUseCase

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

    single { FirebaseMessaging.getInstance() }
    factoryOf(::SyncPushTokenUseCase)
    factoryOf(::DeletePushTokenUseCase)
    workerOf(::SyncPushTokenWorker)
    workerOf(::DeletePushTokenWorker)
}

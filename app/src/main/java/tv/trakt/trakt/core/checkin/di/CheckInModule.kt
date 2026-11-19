package tv.trakt.trakt.core.checkin.di

import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.core.checkin.data.CheckInManager
import tv.trakt.trakt.core.checkin.data.DefaultCheckInManager
import tv.trakt.trakt.core.checkin.data.work.CheckInCompleteWorker

val checkInModule = module {
    singleOf(::DefaultCheckInManager) { bind<CheckInManager>() }
    workerOf(::CheckInCompleteWorker)
}

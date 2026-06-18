package tv.trakt.trakt.core.checkin.di

import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.core.checkin.data.CheckInManager
import tv.trakt.trakt.core.checkin.data.DefaultCheckInManager
import tv.trakt.trakt.core.checkin.data.remote.CheckInApiClient
import tv.trakt.trakt.core.checkin.data.remote.CheckInRemoteDataSource
import tv.trakt.trakt.core.checkin.data.updates.CheckInUpdates
import tv.trakt.trakt.core.checkin.data.updates.CheckInUpdatesStorage

val checkInModule = module {
    singleOf(::DefaultCheckInManager) { bind<CheckInManager>() }
    singleOf(::CheckInApiClient) { bind<CheckInRemoteDataSource>() }
    singleOf(::CheckInUpdatesStorage) { bind<CheckInUpdates>() }
}

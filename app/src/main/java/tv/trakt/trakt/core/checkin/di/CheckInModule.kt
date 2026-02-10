package tv.trakt.trakt.core.checkin.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.CheckinApi
import tv.trakt.trakt.common.Config.API_BASE_URL
import tv.trakt.trakt.core.checkin.data.CheckInManager
import tv.trakt.trakt.core.checkin.data.DefaultCheckInManager
import tv.trakt.trakt.core.checkin.data.remote.CheckInApiClient
import tv.trakt.trakt.core.checkin.data.remote.CheckInRemoteDataSource
import tv.trakt.trakt.core.checkin.data.updates.CheckInUpdates
import tv.trakt.trakt.core.checkin.data.updates.CheckInUpdatesStorage

val checkInModule = module {
    single<CheckInManager> {
        DefaultCheckInManager(
            sessionManager = get(),
            checkInUpdates = get(),
            checkInRemoteDataSource = get(),
            userRemoteDataSource = get(),
            cacheMarkerProvider = get(),
            loadUserProgressUseCase = get(),
            loadUserWatchlistUseCase = get(),
        )
    }

    single<CheckInRemoteDataSource> {
        CheckInApiClient(
            api = CheckinApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = get(),
                httpClientConfig = get(named("authorizedClientConfig")),
            ),
        )
    }

    single<CheckInUpdates> {
        CheckInUpdatesStorage()
    }
}

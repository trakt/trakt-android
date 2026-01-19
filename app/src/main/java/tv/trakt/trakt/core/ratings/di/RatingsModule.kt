package tv.trakt.trakt.core.ratings.di

import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module
import tv.trakt.trakt.core.ratings.PostRatingUseCase
import tv.trakt.trakt.core.ratings.data.RatingsUpdates
import tv.trakt.trakt.core.ratings.data.RatingsUpdatesStorage
import tv.trakt.trakt.core.ratings.data.remote.RatingsApiClient
import tv.trakt.trakt.core.ratings.data.remote.RatingsRemoteDataSource
import tv.trakt.trakt.core.ratings.data.work.PostRatingWorker

internal val ratingsDataModule = module {
    single<RatingsRemoteDataSource> {
        RatingsApiClient(
            ratingsApi = get(),
            cacheMarker = get(),
        )
    }

    single<RatingsUpdates> {
        RatingsUpdatesStorage()
    }
}

internal val ratingsModule = module {
    factory {
        PostRatingUseCase(
            remoteSource = get(),
        )
    }

    worker {
        PostRatingWorker(
            appContext = androidApplication(),
            workerParams = get(),
            sessionManager = get(),
            postRatingUseCase = get(),
            loadUserRatingUseCase = get(),
            ratingsUpdates = get(),
            analytics = get(),
        )
    }
}

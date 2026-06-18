package tv.trakt.trakt.core.reactions.di

import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import tv.trakt.trakt.core.reactions.data.ReactionsUpdates
import tv.trakt.trakt.core.reactions.data.ReactionsUpdatesStorage
import tv.trakt.trakt.core.reactions.data.remote.ReactionsApiClient
import tv.trakt.trakt.core.reactions.data.remote.ReactionsRemoteDataSource
import tv.trakt.trakt.core.reactions.data.work.DeleteReactionWorker
import tv.trakt.trakt.core.reactions.data.work.PostReactionWorker
import tv.trakt.trakt.core.reactions.usecases.DeleteCommentReactionUseCase
import tv.trakt.trakt.core.reactions.usecases.PostCommentReactionUseCase

internal val reactionsDataModule = module {
    singleOf(::ReactionsApiClient) { bind<ReactionsRemoteDataSource>() }
    singleOf(::ReactionsUpdatesStorage) { bind<ReactionsUpdates>() }
}

internal val reactionsModule = module {
    factoryOf(::PostCommentReactionUseCase)
    factoryOf(::DeleteCommentReactionUseCase)

    worker {
        PostReactionWorker(
            appContext = androidApplication(),
            workerParams = get(),
            postReactionUseCase = get(),
            loadUserReactionsUseCase = get(),
            reactionsUpdates = get(),
            sessionManager = get(),
            analytics = get(),
        )
    }

    worker {
        DeleteReactionWorker(
            appContext = androidApplication(),
            workerParams = get(),
            deleteReactionsUseCase = get(),
            loadUserReactionsUseCase = get(),
            reactionsUpdates = get(),
            sessionManager = get(),
            analytics = get(),
        )
    }
}

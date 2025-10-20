package tv.trakt.trakt.core.reactions.di

import org.koin.dsl.module
import tv.trakt.trakt.core.reactions.data.remote.ReactionsApiClient
import tv.trakt.trakt.core.reactions.data.remote.ReactionsRemoteDataSource
import tv.trakt.trakt.core.reactions.usecases.DeleteCommentReactionUseCase
import tv.trakt.trakt.core.reactions.usecases.PostCommentReactionUseCase

internal val reactionsDataModule = module {
    single<ReactionsRemoteDataSource> {
        ReactionsApiClient(
            reactionsApi = get(),
        )
    }
}

internal val reactionsModule = module {
    factory {
        PostCommentReactionUseCase(
            remoteSource = get(),
        )
    }

    factory {
        DeleteCommentReactionUseCase(
            remoteSource = get(),
        )
    }
}

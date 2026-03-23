package tv.trakt.trakt.app.core.comments.di

import androidx.lifecycle.SavedStateHandle
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.app.core.details.comments.CommentDetailsViewModel
import tv.trakt.trakt.common.core.comments.data.remote.CommentsApiClient
import tv.trakt.trakt.common.core.comments.data.remote.CommentsRemoteDataSource
import tv.trakt.trakt.common.core.comments.usecases.GetCommentRepliesUseCase

internal val commentsDataModule = module {
    single<CommentsRemoteDataSource> {
        CommentsApiClient(
            api = get(),
            authorizedApi = get(named("authorizedCommentsApi")),
            cacheMarker = get(),
        )
    }
}

internal val commentsModule = module {

    factory {
        GetCommentRepliesUseCase(
            remoteSource = get(),
        )
    }

    viewModel { (_: SavedStateHandle) ->
        CommentDetailsViewModel(
            getCommentRepliesUseCase = get(),
        )
    }
}

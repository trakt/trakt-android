package tv.trakt.trakt.app.core.comments.di

import androidx.lifecycle.SavedStateHandle
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.CommentsApi
import tv.trakt.trakt.app.Config.API_BASE_URL
import tv.trakt.trakt.app.core.comments.data.remote.CommentsApiClient
import tv.trakt.trakt.app.core.comments.data.remote.CommentsRemoteDataSource
import tv.trakt.trakt.app.core.details.comments.CommentDetailsViewModel
import tv.trakt.trakt.app.core.details.comments.usecases.GetCommentRepliesUseCase

internal val commentsDataModule = module {
    single<CommentsRemoteDataSource> {
        CommentsApiClient(
            api = CommentsApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = get(),
                httpClientConfig = get(named("clientConfig")),
            ),
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

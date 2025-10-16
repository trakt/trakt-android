package tv.trakt.trakt.core.comments.di

import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.CommentsApi
import tv.trakt.trakt.common.Config.API_HD_BASE_URL
import tv.trakt.trakt.common.core.comments.data.remote.CommentsApiClient
import tv.trakt.trakt.common.core.comments.data.remote.CommentsRemoteDataSource
import tv.trakt.trakt.common.core.comments.usecases.GetCommentRepliesUseCase
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.core.comments.CommentsViewModel
import tv.trakt.trakt.core.comments.details.CommentDetailsViewModel

internal val commentsDataModule = module {
    single<CommentsRemoteDataSource> {
        CommentsApiClient(
            api = CommentsApi(
                baseUrl = API_HD_BASE_URL,
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

    viewModel {
        CommentsViewModel(
            savedStateHandle = get(),
            getShowCommentsUseCase = get(),
            getMovieCommentsUseCase = get(),
            getEpisodeCommentsUseCase = get(),
        )
    }

    viewModel { (comment: Comment) ->
        CommentDetailsViewModel(
            comment = comment,
            sessionManager = get(),
            getRepliseUseCase = get(),
        )
    }
}

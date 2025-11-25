package tv.trakt.trakt.core.comments.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.openapitools.client.apis.CommentsApi
import tv.trakt.trakt.common.Config.API_BASE_URL
import tv.trakt.trakt.common.core.comments.data.remote.CommentsApiClient
import tv.trakt.trakt.common.core.comments.data.remote.CommentsRemoteDataSource
import tv.trakt.trakt.common.core.comments.usecases.GetCommentReactionsUseCase
import tv.trakt.trakt.common.core.comments.usecases.GetCommentRepliesUseCase
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.comments.CommentsViewModel
import tv.trakt.trakt.core.comments.data.CommentsUpdates
import tv.trakt.trakt.core.comments.data.CommentsUpdatesStorage
import tv.trakt.trakt.core.comments.features.deletecomment.DeleteCommentViewModel
import tv.trakt.trakt.core.comments.features.details.CommentDetailsViewModel
import tv.trakt.trakt.core.comments.features.postcomment.PostCommentViewModel
import tv.trakt.trakt.core.comments.features.postreply.PostReplyViewModel
import tv.trakt.trakt.core.comments.usecases.DeleteCommentUseCase
import tv.trakt.trakt.core.comments.usecases.GetCommentsFilterUseCase
import tv.trakt.trakt.core.comments.usecases.PostCommentUseCase
import tv.trakt.trakt.core.comments.usecases.PostReplyUseCase

internal const val COMMENTS_PREFERENCES = "comments_preferences_mobile"

internal val commentsDataModule = module {
    single<CommentsRemoteDataSource> {
        CommentsApiClient(
            authedApi = CommentsApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = get(),
                httpClientConfig = get(named("authorizedClientConfig")),
            ),
            api = CommentsApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = get(),
                httpClientConfig = get(named("clientConfig")),
            ),
            cacheMarker = get(),
        )
    }

    single<DataStore<Preferences>>(named(COMMENTS_PREFERENCES)) {
        createStore(
            context = androidApplication(),
        )
    }

    single<CommentsUpdates> {
        CommentsUpdatesStorage()
    }
}

internal val commentsModule = module {
    factory {
        GetCommentRepliesUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetCommentReactionsUseCase(
            remoteSource = get(),
        )
    }

    factory {
        GetCommentsFilterUseCase(
            dataStore = get(named(COMMENTS_PREFERENCES)),
        )
    }

    factory {
        PostCommentUseCase(
            remoteSource = get(),
        )
    }

    factory {
        PostReplyUseCase(
            remoteSource = get(),
        )
    }

    factory {
        DeleteCommentUseCase(
            remoteSource = get(),
        )
    }

    viewModel {
        CommentsViewModel(
            appContext = androidApplication(),
            savedStateHandle = get(),
            getFilterUseCase = get(),
            getShowCommentsUseCase = get(),
            getMovieCommentsUseCase = get(),
            getEpisodeCommentsUseCase = get(),
            getCommentReactionsUseCase = get(),
            getCommentRepliesUseCase = get(),
            sessionManager = get(),
            loadUserReactionsUseCase = get(),
            reactionsUpdates = get(),
            commentsUpdates = get(),
        )
    }

    viewModel { (comment: Comment) ->
        CommentDetailsViewModel(
            appContext = androidApplication(),
            comment = comment,
            sessionManager = get(),
            getRepliesUseCase = get(),
            getCommentReactionsUseCase = get(),
            loadUserReactionsUseCase = get(),
        )
    }

    viewModel { (mediaId: TraktId, mediaType: MediaType) ->
        PostCommentViewModel(
            mediaId = mediaId,
            mediaType = mediaType,
            sessionManager = get(),
            postCommentUseCase = get(),
        )
    }

    viewModel { (comment: Comment) ->
        PostReplyViewModel(
            comment = comment,
            sessionManager = get(),
            postReplyUseCase = get(),
        )
    }

    viewModel { (commentId: TraktId) ->
        DeleteCommentViewModel(
            commentId = commentId,
            deleteCommentUseCase = get(),
        )
    }
}

private fun createStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler(
            produceNewData = { emptyPreferences() },
        ),
        migrations = listOf(SharedPreferencesMigration(context, COMMENTS_PREFERENCES)),
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { context.preferencesDataStoreFile(COMMENTS_PREFERENCES) },
    )
}

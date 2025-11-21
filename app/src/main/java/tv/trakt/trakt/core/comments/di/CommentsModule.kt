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
import tv.trakt.trakt.core.comments.features.details.CommentDetailsViewModel
import tv.trakt.trakt.core.comments.features.postcomment.PostCommentViewModel
import tv.trakt.trakt.core.comments.usecases.GetCommentsFilterUseCase
import tv.trakt.trakt.core.comments.usecases.PostCommentUseCase

internal const val COMMENTS_PREFERENCES = "comments_preferences_mobile"

internal val commentsDataModule = module {
    single<CommentsRemoteDataSource> {
        CommentsApiClient(
            api = CommentsApi(
                baseUrl = API_BASE_URL,
                httpClientEngine = get(),
                httpClientConfig = get(named("authorizedClientConfig")),
            ),
            cacheMarker = get(),
        )
    }

    single<DataStore<Preferences>>(named(COMMENTS_PREFERENCES)) {
        createStore(
            context = androidApplication(),
        )
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

    viewModel {
        CommentsViewModel(
            appContext = androidApplication(),
            savedStateHandle = get(),
            getFilterUseCase = get(),
            getShowCommentsUseCase = get(),
            getMovieCommentsUseCase = get(),
            getEpisodeCommentsUseCase = get(),
            getCommentReactionsUseCase = get(),
            sessionManager = get(),
            loadUserReactionsUseCase = get(),
            reactionsUpdates = get(),
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

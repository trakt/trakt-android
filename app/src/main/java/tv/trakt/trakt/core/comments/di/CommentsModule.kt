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
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tv.trakt.trakt.common.core.comments.data.remote.CommentsApiClient
import tv.trakt.trakt.common.core.comments.data.remote.CommentsRemoteDataSource
import tv.trakt.trakt.common.core.comments.usecases.GetCommentReactionsUseCase
import tv.trakt.trakt.common.core.comments.usecases.GetCommentRepliesUseCase
import tv.trakt.trakt.common.model.Comment
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.comments.CommentsViewModel
import tv.trakt.trakt.core.comments.data.CommentsUpdates
import tv.trakt.trakt.core.comments.data.CommentsUpdatesStorage
import tv.trakt.trakt.core.comments.features.deletecomment.DeleteCommentViewModel
import tv.trakt.trakt.core.comments.features.details.CommentDetailsViewModel
import tv.trakt.trakt.core.comments.features.editcomment.EditCommentViewModel
import tv.trakt.trakt.core.comments.features.postcomment.PostCommentViewModel
import tv.trakt.trakt.core.comments.features.postreply.PostReplyViewModel
import tv.trakt.trakt.core.comments.features.report.ReportCommentViewModel
import tv.trakt.trakt.core.comments.features.translation.data.CommentTranslationsStore
import tv.trakt.trakt.core.comments.features.translation.data.CommentTranslator
import tv.trakt.trakt.core.comments.features.translation.data.MlKitCommentTranslator
import tv.trakt.trakt.core.comments.features.translation.download.CommentTranslationDownloadViewModel
import tv.trakt.trakt.core.comments.model.MentionSource
import tv.trakt.trakt.core.comments.usecases.DeleteCommentUseCase
import tv.trakt.trakt.core.comments.usecases.EditCommentUseCase
import tv.trakt.trakt.core.comments.usecases.GetCommentMentionsUseCase
import tv.trakt.trakt.core.comments.usecases.GetCommentsFilterUseCase
import tv.trakt.trakt.core.comments.usecases.GetCommentsLanguageUseCase
import tv.trakt.trakt.core.comments.usecases.PostCommentUseCase
import tv.trakt.trakt.core.comments.usecases.PostReplyUseCase
import tv.trakt.trakt.core.comments.usecases.ReportCommentUseCase

internal const val COMMENTS_PREFERENCES = "comments_preferences_mobile"

internal val commentsDataModule = module {
    single<CommentsRemoteDataSource> {
        CommentsApiClient(
            api = get(),
            authorizedApi = get(named("authorizedCommentsApi")),
            cacheMarker = get(),
        )
    }

    single<DataStore<Preferences>>(named(COMMENTS_PREFERENCES)) {
        createStore(
            context = androidApplication(),
        )
    }

    singleOf(::CommentsUpdatesStorage) { bind<CommentsUpdates>() }
    singleOf(::MlKitCommentTranslator) { bind<CommentTranslator>() }
    singleOf(::CommentTranslationsStore)
}

internal val commentsModule = module {
    viewModelOf(::CommentTranslationDownloadViewModel)

    factoryOf(::GetCommentRepliesUseCase)
    factoryOf(::GetCommentReactionsUseCase)
    factoryOf(::PostCommentUseCase)
    factoryOf(::EditCommentUseCase)
    factoryOf(::GetCommentMentionsUseCase)
    factoryOf(::PostReplyUseCase)
    factoryOf(::DeleteCommentUseCase)
    factoryOf(::ReportCommentUseCase)

    factory {
        GetCommentsFilterUseCase(
            dataStore = get(named(COMMENTS_PREFERENCES)),
        )
    }

    factory {
        GetCommentsLanguageUseCase(
            dataStore = get(named(COMMENTS_PREFERENCES)),
        )
    }

    viewModel {
        CommentsViewModel(
            appContext = androidApplication(),
            savedStateHandle = get(),
            getFilterUseCase = get(),
            getLanguageUseCase = get(),
            getShowCommentsUseCase = get(),
            getMovieCommentsUseCase = get(),
            getEpisodeCommentsUseCase = get(),
            getCommentReactionsUseCase = get(),
            getCommentRepliesUseCase = get(),
            sessionManager = get(),
            loadUserReactionsUseCase = get(),
            reactionsUpdates = get(),
            commentsUpdates = get(),
            translationsStore = get(),
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
            commentsUpdates = get(),
            translationsStore = get(),
        )
    }

    viewModel { (mediaId: TraktId, mediaType: MediaType, mentionSource: MentionSource?) ->
        PostCommentViewModel(
            mediaId = mediaId,
            mediaType = mediaType,
            sessionManager = get(),
            postCommentUseCase = get(),
            mentionSource = mentionSource,
            getCommentMentionsUseCase = get(),
            analytics = get(),
        )
    }

    viewModel { (comment: Comment, mentionSource: MentionSource?) ->
        EditCommentViewModel(
            comment = comment,
            editCommentUseCase = get(),
            mentionSource = mentionSource,
            getCommentMentionsUseCase = get(),
        )
    }

    viewModel { (comment: Comment, user: User?, mentionSource: MentionSource?) ->
        PostReplyViewModel(
            comment = comment,
            commentUser = user,
            sessionManager = get(),
            postReplyUseCase = get(),
            mentionSource = mentionSource,
            getCommentMentionsUseCase = get(),
            analytics = get(),
        )
    }

    viewModel { (commentId: TraktId) ->
        DeleteCommentViewModel(
            commentId = commentId,
            deleteCommentUseCase = get(),
            analytics = get(),
        )
    }

    viewModel { (comment: Comment) ->
        ReportCommentViewModel(
            comment = comment,
            reportCommentUseCase = get(),
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

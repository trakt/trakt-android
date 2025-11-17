package tv.trakt.trakt.core.reactions.data.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import tv.trakt.trakt.analytics.Analytics
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.core.reactions.data.ReactionsUpdates
import tv.trakt.trakt.core.reactions.usecases.DeleteCommentReactionUseCase
import tv.trakt.trakt.core.user.usecases.reactions.LoadUserReactionsUseCase
import java.util.concurrent.TimeUnit.SECONDS

private const val MAX_RETRY_ATTEMPTS = 2

internal class DeleteReactionWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    val sessionManager: SessionManager,
    val deleteReactionsUseCase: DeleteCommentReactionUseCase,
    val loadUserReactionsUseCase: LoadUserReactionsUseCase,
    val reactionsUpdates: ReactionsUpdates,
    val analytics: Analytics,
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        fun scheduleOneTime(
            appContext: Context,
            commentId: Int,
            source: ReactionsUpdates.Source? = null,
        ) {
            val workRequest = OneTimeWorkRequestBuilder<DeleteReactionWorker>()
                .setInputData(
                    Data.Builder()
                        .putInt("commentId", commentId)
                        .putString("source", source?.name)
                        .build(),
                )
                .setBackoffCriteria(BackoffPolicy.LINEAR, 3, SECONDS)
                .build()

            WorkManager
                .getInstance(appContext)
                .enqueueUniqueWork(
                    "delete_reaction_$commentId",
                    ExistingWorkPolicy.REPLACE,
                    workRequest,
                )
        }
    }

    override suspend fun doWork(): Result {
        try {
            if (!sessionManager.isAuthenticated()) {
                Timber.d("Not authenticated, cannot post reaction")
                return Result.failure()
            }

            if (runAttemptCount >= MAX_RETRY_ATTEMPTS) {
                Timber.d("Max retry attempts reached, failing work")
                return Result.failure()
            }

            val reactionSource = inputData.getString("source")
            val commentId = inputData.getInt("commentId", -1)
            if (commentId == -1) {
                Timber.d("Invalid comment ID, cannot delete reaction")
                return Result.failure()
            }

            withContext(Dispatchers.IO) {
                deleteReactionsUseCase.deleteReactions(
                    commentId = inputData.getInt("commentId", -1),
                )

                analytics.reactions.logReactionRemove(
                    source = reactionSource ?: "unknown",
                )

                loadUserReactionsUseCase.loadReactions()

                if (!reactionSource.isNullOrBlank()) {
                    val source = ReactionsUpdates.Source.valueOf(reactionSource)
                    reactionsUpdates.notifyUpdate(
                        commentId = commentId,
                        source = source,
                    )
                }
            }
        } catch (error: Exception) {
            if (error is CancellationException) {
                return Result.failure()
            }
            Timber.recordError(error)
            return Result.retry()
        }

        Timber.d("Successfully deleted reaction.")
        return Result.success()
    }
}

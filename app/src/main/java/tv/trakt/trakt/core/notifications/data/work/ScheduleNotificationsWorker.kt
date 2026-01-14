package tv.trakt.trakt.core.notifications.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem.EpisodeItem
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem.MovieItem
import tv.trakt.trakt.core.home.sections.upcoming.usecases.GetUpcomingUseCase
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.notifications.TraktNotificationChannel
import tv.trakt.trakt.core.notifications.model.PostNotificationData
import tv.trakt.trakt.core.settings.usecases.EnableNotificationsUseCase
import tv.trakt.trakt.resources.R
import java.time.temporal.ChronoUnit

private const val WORK_ID = "schedule_notifications_work"

internal class ScheduleNotificationsWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    val sessionManager: SessionManager,
    val getUpcomingUseCase: GetUpcomingUseCase,
    val enableNotificationsUseCase: EnableNotificationsUseCase,
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        fun schedule(appContext: Context) {
            val workRequest = OneTimeWorkRequestBuilder<ScheduleNotificationsWorker>()
                .build()

            with(WorkManager.getInstance(appContext)) {
                cancelAllWorkByTag(WORK_NOTIFICATION_TAG)
                enqueueUniqueWork(
                    WORK_ID,
                    ExistingWorkPolicy.REPLACE,
                    workRequest,
                )
            }
        }

        fun clear(appContext: Context) {
            with(WorkManager.getInstance(appContext)) {
                cancelAllWorkByTag(WORK_NOTIFICATION_TAG)
            }
        }
    }

    override suspend fun doWork(): Result {
        try {
            if (!sessionManager.isAuthenticated()) {
                Timber.d("Not authenticated, skipping.")
                return Result.failure()
            }

            if (!enableNotificationsUseCase.isNotificationsEnabled()) {
                Timber.d("Notifications are disabled, skipping.")
                return Result.failure()
            }

            var upcomingItems = getUpcomingUseCase.getLocalUpcoming(MediaMode.MEDIA)
            if (upcomingItems.isEmpty()) {
                upcomingItems = getUpcomingUseCase.getUpcoming(MediaMode.MEDIA)
            }

            val nowUtc = nowUtcInstant()
            upcomingItems
                .filter { it.releasedAt.isAfter(nowUtc) }
                .forEach {
                    postNotification(it)
                }
        } catch (error: Exception) {
            if (error is CancellationException) {
                return Result.failure()
            }
            Timber.recordError(error)
            return Result.failure()
        }

        Timber.d("Successfully scheduled notifications.")
        return Result.success()
    }

    private fun postNotification(item: HomeUpcomingItem) {
        PostNotificationWorker.schedule(
            appContext = applicationContext,
            data = PostNotificationData(
                channel = when (item) {
                    is EpisodeItem -> TraktNotificationChannel.SHOWS
                    is MovieItem -> TraktNotificationChannel.MOVIES
                },
                mediaId = item.id.value,
                mediaType = when (item) {
                    is EpisodeItem -> MediaType.EPISODE
                    is MovieItem -> MediaType.MOVIE
                },
                mediaImage = item.images?.getPosterUrl(),
                title = when (item) {
                    is EpisodeItem -> item.show.title
                    is MovieItem -> item.movie.title
                },
                content = when (item) {
                    is EpisodeItem -> {
                        applicationContext.getString(
                            R.string.text_notification_episode_release,
                            item.episode.season,
                            item.episode.number,
                        )
                    }
                    is MovieItem -> {
                        applicationContext.getString(R.string.text_notification_movie_release)
                    }
                },
                targetDate = item.releasedAt.truncatedTo(ChronoUnit.MINUTES),
//                targetDate = nowUtcInstant().plusSeconds(5),
            ),
        )
    }
}

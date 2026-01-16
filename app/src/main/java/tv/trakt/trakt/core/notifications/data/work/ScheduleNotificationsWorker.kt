package tv.trakt.trakt.core.notifications.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem.EpisodeItem
import tv.trakt.trakt.core.home.sections.upcoming.model.HomeUpcomingItem.MovieItem
import tv.trakt.trakt.core.home.sections.upcoming.usecases.GetUpcomingUseCase
import tv.trakt.trakt.core.main.model.MediaMode
import tv.trakt.trakt.core.notifications.TraktNotificationChannel
import tv.trakt.trakt.core.notifications.model.DeliveryAdjustment
import tv.trakt.trakt.core.notifications.model.PostNotificationData
import tv.trakt.trakt.core.notifications.usecases.EnableNotificationsUseCase
import tv.trakt.trakt.core.notifications.usecases.UpdateNotificationsDeliveryUseCase
import tv.trakt.trakt.resources.R
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private const val WORK_ID = "schedule_notifications_work"

internal class ScheduleNotificationsWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    val sessionManager: SessionManager,
    val getUpcomingUseCase: GetUpcomingUseCase,
    val enableNotificationsUseCase: EnableNotificationsUseCase,
    val notificationsDeliveryUseCase: UpdateNotificationsDeliveryUseCase,
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        fun schedule(
            appContext: Context,
            forceRemote: Boolean = false,
        ) {
            val workRequest = OneTimeWorkRequestBuilder<ScheduleNotificationsWorker>()
                .setInputData(
                    Data.Builder()
                        .putBoolean("forceRemote", forceRemote)
                        .build(),
                )
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
        val forceRemote = inputData.getBoolean("forceRemote", false)
        Timber.d("Scheduling notifications (forceRemote=$forceRemote)...")

        try {
            if (!sessionManager.isAuthenticated()) {
                Timber.d("Not authenticated, skipping.")
                return Result.failure()
            }

            if (!enableNotificationsUseCase.isNotificationsEnabled()) {
                Timber.d("Notifications are disabled, skipping.")
                return Result.failure()
            }

            delay(3.seconds)
            var upcomingItems = getUpcomingUseCase.getLocalUpcoming(MediaMode.MEDIA)
            if (upcomingItems.isEmpty() || forceRemote) {
                try {
                    upcomingItems = getUpcomingUseCase.getUpcoming(MediaMode.MEDIA)
                } catch (error: Exception) {
                    Timber.recordError(error)
                }
            }

            if (upcomingItems.isEmpty()) {
                Timber.d("No upcoming items found, skipping.")
                return Result.success()
            }

            val nowUtc = nowUtcInstant()
            val deliveryAdjustment = notificationsDeliveryUseCase.getDeliveryTime()

            val futureItems = upcomingItems
                .filter { it.releasedAt.isAfter(nowUtc) }

            // Post notifications for movies
            futureItems
                .filterIsInstance<MovieItem>()
                .forEach { movie ->
                    postNotification(
                        item = movie,
                        deliveryAdjustment = deliveryAdjustment,
                    )
                }

            // Post notifications for episodes
            val episodeGroups = futureItems
                .filterIsInstance<EpisodeItem>()
                .groupBy { episode ->
                    val showId = episode.show.ids.trakt.value
                    val releaseDate = episode.releasedAt.truncatedTo(ChronoUnit.MINUTES)
                    showId to releaseDate
                }

            episodeGroups.forEach { (_, episodes) ->
                if (episodes.size > 1) {
                    // Multiple episodes for same show on same date
                    postMultiEpisodesNotification(
                        episodes = episodes.sortedBy { it.episode.number },
                        deliveryAdjustment = deliveryAdjustment,
                    )
                } else {
                    // Single episode
                    postNotification(
                        item = episodes.first(),
                        deliveryAdjustment = deliveryAdjustment,
                    )
                }
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

    private fun postNotification(
        item: HomeUpcomingItem,
        deliveryAdjustment: DeliveryAdjustment,
    ) {
        var targetDate = item.releasedAt.truncatedTo(ChronoUnit.MINUTES).toLocal()
        if (deliveryAdjustment != DeliveryAdjustment.DISABLED) {
            Timber.d("Applying delivery adjustment: ${deliveryAdjustment.duration}")
            targetDate = targetDate.minus(deliveryAdjustment.duration.toJavaDuration())
        }

        // Adjust notifications scheduled between 00:00 and 06:00 to 10:00 local time.
//        if (targetDate.hour in 0..6) {
//            val adjustedDate = targetDate.withHour(10).withMinute(0)
//            Timber.d("Date adjusted: ${item.id.value} from $targetDate to $adjustedDate")
//            targetDate = adjustedDate
//        }

        PostNotificationWorker.schedule(
            appContext = applicationContext,
            data = PostNotificationData(
                targetDate = targetDate.toInstant(),
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
                extraId = when (item) {
                    is EpisodeItem -> item.show.ids.trakt.value
                    is MovieItem -> null
                },
                extraValue1 = when (item) {
                    is EpisodeItem -> item.episode.season
                    is MovieItem -> null
                },
                extraValue2 = when (item) {
                    is EpisodeItem -> item.episode.number
                    is MovieItem -> null
                },
            ),
        )
    }

    private fun postMultiEpisodesNotification(
        episodes: List<EpisodeItem>,
        deliveryAdjustment: DeliveryAdjustment,
    ) {
        val firstEpisode = episodes.first()
        val lastEpisode = episodes.last()

        var targetDate = firstEpisode.releasedAt.truncatedTo(ChronoUnit.MINUTES).toLocal()
        if (deliveryAdjustment != DeliveryAdjustment.DISABLED) {
            Timber.d("Applying delivery adjustment: ${deliveryAdjustment.duration}")
            targetDate = targetDate.minus(deliveryAdjustment.duration.toJavaDuration())
        }

        // Adjust notifications scheduled between 00:00 and 06:00 to 10:00 local time.
//        if (targetDate.hour in 0..6) {
//            val adjustedDate = targetDate.withHour(10).withMinute(0)
//            Timber.d("Date adjusted: grouped episodes from $targetDate to $adjustedDate")
//            targetDate = adjustedDate
//        }

        PostNotificationWorker.schedule(
            appContext = applicationContext,
            data = PostNotificationData(
                targetDate = targetDate.toInstant(),
                channel = TraktNotificationChannel.SHOWS,
                mediaId = firstEpisode.id.value,
                mediaType = MediaType.EPISODE,
                mediaImage = firstEpisode.images?.getPosterUrl(),
                title = firstEpisode.show.title,
                content = applicationContext.getString(
                    R.string.text_notification_episodes_release,
                    firstEpisode.episode.season,
                    firstEpisode.episode.number,
                    lastEpisode.episode.number,
                ),
                extraId = firstEpisode.show.ids.trakt.value,
                extraValue1 = firstEpisode.episode.season,
                extraValue2 = firstEpisode.episode.number,
            ),
        )
    }
}

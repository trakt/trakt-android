package tv.trakt.trakt.core.notifications.data.work.mir

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Notification
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.serialization.json.Json
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.helpers.extensions.nowLocal
import tv.trakt.trakt.common.ui.theme.colors.Purple500
import tv.trakt.trakt.core.notifications.TraktNotificationChannel
import tv.trakt.trakt.core.notifications.data.work.MAIN_ACTIVITY_PATH
import tv.trakt.trakt.core.notifications.model.MonthReviewIntentExtras
import tv.trakt.trakt.core.notifications.usecases.EnableNotificationsUseCase
import tv.trakt.trakt.resources.R
import java.time.Duration
import java.time.YearMonth
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit.DAYS
import java.util.Locale
import java.util.concurrent.TimeUnit.MILLISECONDS

private const val WORK_ID = "post_month_review_notif_work"
private const val NOTIFICATION_HOUR = 10

private const val KEY_YEAR = "year"
private const val KEY_MONTH = "month"
private const val KEY_USER_SLUG = "userSlug"

internal const val INTENT_NOTIFICATION_MONTH_REVIEW_EXTRAS = "NOTIFICATION_INTENT_MONTH_REVIEW_EXTRAS"

internal class PostMonthReviewNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val sessionManager: SessionManager,
    private val enableNotificationsUseCase: EnableNotificationsUseCase,
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        fun schedule(
            appContext: Context,
            userSlug: String,
        ) {
            val now = nowLocal()
            val targetDate = nextNotificationDate(now)
            val reviewMonth = YearMonth.from(targetDate).minusMonths(1)

            val workRequest = OneTimeWorkRequestBuilder<PostMonthReviewNotificationWorker>()
                .addTag(WORK_MONTH_REVIEW_TAG)
                .setInputData(
                    Data.Builder()
                        .putInt(KEY_YEAR, reviewMonth.year)
                        .putInt(KEY_MONTH, reviewMonth.monthValue)
                        .putString(KEY_USER_SLUG, userSlug)
                        .build(),
                )
                .setInitialDelay(Duration.between(now, targetDate).toMillis(), MILLISECONDS)
                .build()

            WorkManager.getInstance(appContext).enqueueUniqueWork(
                WORK_ID,
                ExistingWorkPolicy.REPLACE,
                workRequest,
            )

            Timber.d("Month in review notification scheduled for %s at %s.", reviewMonth, targetDate)
        }

        private fun nextNotificationDate(now: ZonedDateTime): ZonedDateTime {
            val thisMonth = now
                .withDayOfMonth(1)
                .truncatedTo(DAYS)
                .withHour(NOTIFICATION_HOUR)

            return if (thisMonth.isAfter(now)) thisMonth else thisMonth.plusMonths(1)
        }
    }

    override suspend fun doWork(): Result {
        val year = inputData.getInt(KEY_YEAR, -1)
        val month = inputData.getInt(KEY_MONTH, -1)
        val userSlug = inputData.getString(KEY_USER_SLUG)

        if (year == -1 || month == -1 || userSlug.isNullOrBlank()) {
            Timber.e("Invalid month in review notification data")
            return Result.failure()
        }

        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(applicationContext, POST_NOTIFICATIONS) == PERMISSION_GRANTED

        if (!hasPermission) {
            Timber.e("Missing POST_NOTIFICATIONS permission for month in review notification")
            return Result.failure()
        }

        if (!enableNotificationsUseCase.isNotificationsEnabled()) {
            Timber.d("Notifications are disabled, skipping month in review notification.")
            return Result.failure()
        }

        if (sessionManager.getProfile()?.isAnyVip != true) {
            Timber.d("User is not VIP, skipping month in review notification.")
            return Result.failure()
        }

        val reviewMonth = YearMonth.of(year, month)
        val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
        val monthName = reviewMonth.month
            .getDisplayName(TextStyle.FULL_STANDALONE, locale)
            .replaceFirstChar { it.titlecase(locale) }

        val notification = Notification
            .Builder(applicationContext, TraktNotificationChannel.MONTH_REVIEW.id)
            .setContentIntent(createNotificationIntent(reviewMonth, userSlug))
            .setSmallIcon(R.drawable.ic_trakt_icon_notification)
            .setContentTitle(applicationContext.getString(R.string.mir_title_month_in_review))
            .setContentText(applicationContext.getString(R.string.mir_banner_body_short, monthName))
            .setAutoCancel(true)
            .setColor(Purple500.toArgb())
            .build()

        NotificationManagerCompat
            .from(applicationContext)
            .notify("$WORK_ID-$reviewMonth".hashCode(), notification)

        return Result.success()
    }

    private fun createNotificationIntent(
        reviewMonth: YearMonth,
        userSlug: String,
    ): PendingIntent {
        val notifyIntent = Intent(applicationContext, Class.forName(MAIN_ACTIVITY_PATH)).apply {
            putExtra(
                INTENT_NOTIFICATION_MONTH_REVIEW_EXTRAS,
                Json.encodeToString(
                    MonthReviewIntentExtras(
                        year = reviewMonth.year,
                        month = reviewMonth.monthValue,
                        userSlug = userSlug,
                    ),
                ),
            )
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        return PendingIntent.getActivity(
            applicationContext,
            "$WORK_ID-$reviewMonth".hashCode(),
            notifyIntent,
            FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT,
        )
    }
}

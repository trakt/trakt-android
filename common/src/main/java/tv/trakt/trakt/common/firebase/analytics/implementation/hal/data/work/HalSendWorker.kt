package tv.trakt.trakt.common.firebase.analytics.implementation.hal.data.work

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import timber.log.Timber
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.data.HalEvent
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.data.HalEventStore
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.data.encodeHalBatch
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.data.remote.HalDelivery
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.data.remote.HalSender
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private const val WORK_NAME = "hal_send"
private const val MAX_RETRY_ATTEMPTS = 3
private const val MAX_BATCH_SIZE = 50

private val SEND_DELAY = 10.seconds
private val BACKOFF_DELAY = 5.seconds

/**
 * Drains [HalEventStore] in batches. Events stay queued until HAL accepts or rejects them.
 */
internal class HalSendWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val store: HalEventStore,
    private val sender: HalSender,
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        fun schedule(appContext: Context) {
            val workRequest = OneTimeWorkRequestBuilder<HalSendWorker>()
                .setInitialDelay(SEND_DELAY.toJavaDuration())
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_DELAY.toJavaDuration())
                .build()

            // KEEP lets a pending request batch every event tracked before it runs.
            WorkManager
                .getInstance(appContext)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, workRequest)
        }
    }

    override suspend fun doWork(): Result {
        return when (drain()) {
            HalDelivery.Failed -> retryOrGiveUp()
            HalDelivery.Accepted, HalDelivery.Rejected -> Result.success()
        }
    }

    private tailrec suspend fun drain(): HalDelivery {
        val batch = store.peek(MAX_BATCH_SIZE)
        if (batch.isEmpty()) return HalDelivery.Accepted

        val delivery = send(batch)
        if (delivery == HalDelivery.Failed) return delivery

        store.remove(batch)
        return drain()
    }

    private suspend fun send(batch: List<HalEvent>): HalDelivery {
        return try {
            sender.send(encodeHalBatch(sentAt = System.currentTimeMillis(), events = batch))
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Timber.d(error, "HAL batch not delivered")
            HalDelivery.Failed
        }
    }

    // Undelivered events stay queued; the next tracked event schedules a new attempt.
    private fun retryOrGiveUp(): Result {
        if (runAttemptCount + 1 < MAX_RETRY_ATTEMPTS) return Result.retry()
        return Result.success()
    }
}

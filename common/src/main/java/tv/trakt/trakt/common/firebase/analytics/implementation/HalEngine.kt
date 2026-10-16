package tv.trakt.trakt.common.firebase.analytics.implementation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal const val HAL_MAX_BATCH_SIZE = 50
internal const val HAL_USER_TYPE_DIM = "user_type"

private val DEFAULT_FLUSH_DELAY = 10.seconds

internal interface HalEvents {
    fun track(
        name: String,
        dims: Map<String, String> = emptyMap(),
        metrics: Map<String, Int> = emptyMap(),
    )

    fun setUserType(userType: String?)
}

internal class HalEngine(
    private val sender: HalSender,
    private val scope: CoroutineScope,
    private val flushDelay: Duration = DEFAULT_FLUSH_DELAY,
    private val clock: () -> Long = System::currentTimeMillis,
) : HalEvents {
    private val lock = Any()
    private var buffered = emptyList<HalEvent>()
    private var flushTimer: Job? = null

    @Volatile
    private var userType: String? = null

    override fun setUserType(userType: String?) {
        this.userType = userType
    }

    override fun track(
        name: String,
        dims: Map<String, String>,
        metrics: Map<String, Int>,
    ) {
        val currentUserType = userType
        val event = HalEvent(
            name = name,
            at = clock(),
            dims = if (currentUserType == null) dims else dims + (HAL_USER_TYPE_DIM to currentUserType),
            metrics = metrics,
        )

        val isFull = synchronized(lock) {
            buffered = buffered + event
            if (buffered.size < HAL_MAX_BATCH_SIZE && flushTimer == null) {
                flushTimer = scope.launch {
                    delay(flushDelay)
                    flush()
                }
            }
            buffered.size >= HAL_MAX_BATCH_SIZE
        }

        if (isFull) {
            flush()
        }
    }

    fun flush() {
        val batch = synchronized(lock) {
            flushTimer?.cancel()
            flushTimer = null
            buffered.also { buffered = emptyList() }
        }
        if (batch.isEmpty()) return

        scope.launch {
            batch.chunked(HAL_MAX_BATCH_SIZE).forEach { chunk ->
                try {
                    sender.send(encodeHalBatch(sentAt = clock(), events = chunk))
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    Timber.d(error, "HAL batch dropped")
                }
            }
        }
    }
}

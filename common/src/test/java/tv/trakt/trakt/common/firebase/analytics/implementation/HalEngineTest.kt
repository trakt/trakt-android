package tv.trakt.trakt.common.firebase.analytics.implementation

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

private class RecordingSender(
    private val failWith: Exception? = null,
) : HalSender {
    val bodies = CopyOnWriteArrayList<String>()

    override suspend fun send(body: String) {
        bodies += body
        failWith?.let { throw it }
    }
}

private fun JsonObject.events(): JsonArray = getValue("events").jsonArray

class HalEngineTest {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)

    @After
    fun tearDown() {
        scope.cancel()
    }

    private fun engine(
        sender: HalSender,
        flushDelay: Duration = 10.minutes,
    ) = HalEngine(
        sender = sender,
        scope = scope,
        flushDelay = flushDelay,
        clock = { 1_000L },
    )

    private fun parse(body: String): JsonObject = Json.parseToJsonElement(body).jsonObject

    @Test
    fun `batch payload follows the wire contract`() {
        val body = encodeHalBatch(
            sentAt = 2_000L,
            events = listOf(
                HalEvent(
                    name = "media-action.rate",
                    at = 1_500L,
                    dims = mapOf("action" to "added", "rating" to "8"),
                    metrics = mapOf("rating" to 8),
                ),
                HalEvent(name = "remove-from-history", at = 1_600L, dims = emptyMap(), metrics = emptyMap()),
            ),
        )

        val json = parse(body)
        assertEquals(2_000L, json.getValue("sentAt").jsonPrimitive.long)
        val first = json.events()[0].jsonObject
        assertEquals("media-action.rate", first.getValue("event").jsonPrimitive.content)
        assertEquals(1_500L, first.getValue("at").jsonPrimitive.long)
        assertEquals("added", first.getValue("dims").jsonObject.getValue("action").jsonPrimitive.content)
        assertEquals(8, first.getValue("metrics").jsonObject.getValue("rating").jsonPrimitive.int)
        assertEquals(2, json.events().size)
    }

    @Test
    fun `nothing is sent before the flush delay`() {
        val sender = RecordingSender()
        val engine = engine(sender)

        engine.track("remove-from-history")

        assertTrue(sender.bodies.isEmpty())
    }

    @Test
    fun `flush sends the buffered events once`() {
        val sender = RecordingSender()
        val engine = engine(sender)

        engine.track("remove-from-history")
        engine.track("media-action.delete-comment")
        engine.flush()
        engine.flush()

        assertEquals(1, sender.bodies.size)
        assertEquals(2, parse(sender.bodies.single()).events().size)
    }

    @Test
    fun `the 50th event flushes immediately`() {
        val sender = RecordingSender()
        val engine = engine(sender)

        repeat(HAL_MAX_BATCH_SIZE - 1) { engine.track("remove-from-history") }
        assertTrue(sender.bodies.isEmpty())

        engine.track("remove-from-history")

        assertEquals(1, sender.bodies.size)
        assertEquals(HAL_MAX_BATCH_SIZE, parse(sender.bodies.single()).events().size)
    }

    @Test
    fun `the buffer restarts after a flush`() {
        val sender = RecordingSender()
        val engine = engine(sender)

        repeat(HAL_MAX_BATCH_SIZE + 3) { engine.track("remove-from-history") }
        engine.flush()

        assertEquals(listOf(HAL_MAX_BATCH_SIZE, 3), sender.bodies.map { parse(it).events().size })
    }

    @Test
    fun `the first buffered event schedules a flush`() {
        val sent = CompletableDeferred<String>()
        val engine = engine(
            sender = { sent.complete(it) },
            flushDelay = 20.milliseconds,
        )

        engine.track("remove-from-history")

        val body = runBlocking { withTimeout(5_000) { sent.await() } }
        assertEquals(1, parse(body).events().size)
    }

    @Test
    fun `user type is added to every event once known`() {
        val sender = RecordingSender()
        val engine = engine(sender)

        engine.track("remove-from-history")
        engine.setUserType("vip")
        engine.track("media-action.watchlist", dims = mapOf("action" to "add"))
        engine.flush()

        val events = parse(sender.bodies.single()).events().map { it.jsonObject.getValue("dims").jsonObject }
        assertEquals(null, events[0]["user_type"])
        assertEquals("vip", events[1].getValue("user_type").jsonPrimitive.content)
        assertEquals("add", events[1].getValue("action").jsonPrimitive.content)
    }

    @Test
    fun `a failed send is dropped without retry`() {
        val sender = RecordingSender(failWith = RuntimeException("offline"))
        val engine = engine(sender)

        engine.track("remove-from-history")
        engine.flush()
        engine.flush()

        assertEquals(1, sender.bodies.size)
    }
}

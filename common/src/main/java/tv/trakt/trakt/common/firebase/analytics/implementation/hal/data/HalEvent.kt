package tv.trakt.trakt.common.firebase.analytics.implementation.hal.data

import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal data class HalEvent(
    val name: String,
    val at: Long,
    val dims: Map<String, String>,
    val metrics: Map<String, Int>,
)

private fun Map<String, String>.toDimsJson() =
    buildJsonObject {
        forEach { (key, value) -> put(key, value) }
    }

private fun Map<String, Int>.toMetricsJson() =
    buildJsonObject {
        forEach { (key, value) -> put(key, value) }
    }

internal fun encodeHalBatch(
    sentAt: Long,
    events: List<HalEvent>,
): String {
    return buildJsonObject {
        put("sentAt", sentAt)
        put(
            "events",
            buildJsonArray {
                events.forEach { event ->
                    add(
                        buildJsonObject {
                            put("event", event.name)
                            put("at", event.at)
                            put("dims", event.dims.toDimsJson())
                            put("metrics", event.metrics.toMetricsJson())
                        },
                    )
                }
            },
        )
    }.toString()
}

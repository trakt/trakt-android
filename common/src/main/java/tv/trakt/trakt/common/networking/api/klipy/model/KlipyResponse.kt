package tv.trakt.trakt.common.networking.api.klipy.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Envelope every KLIPY endpoint wraps its payload in.
 */
@Immutable
@Serializable
data class KlipyResponse<T>(
    val result: Boolean = false,
    val data: T? = null,
    val message: String? = null,
)

/**
 * Paged payload carried by the trending and search endpoints. KLIPY nests the item array
 * under a second `data` key next to the pagination markers.
 */
@Immutable
@Serializable
data class KlipyPageDto<T>(
    val data: List<T> = emptyList(),
    @SerialName("current_page")
    val currentPage: Int = 1,
    @SerialName("per_page")
    val perPage: Int = 0,
    @SerialName("has_next")
    val hasNext: Boolean = false,
)

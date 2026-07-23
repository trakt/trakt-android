package tv.trakt.trakt.common.model.globalfilter

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
enum class GlobalFilterRuntime(
    val runtime: Pair<Int, Int>,
) {
    Runtime0To30(0 to 30),
    Runtime30To60(31 to 60),
    Runtime60To90(61 to 90),
    Runtime90To120(91 to 120),
    Runtime120Plus(121 to 500),
}

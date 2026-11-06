package tv.trakt.trakt.core.settings.features.history.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class CleanUpProgress(
    val target: CleanUpTarget,
    val processed: Int = 0,
    val total: Int = 0,
    val removed: Int? = null,
) {
    val isRunning: Boolean
        get() = removed == null
}

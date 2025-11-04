package tv.trakt.trakt.core.favorites

import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal interface FavoritesUpdates {
    fun notifyUpdate(source: Source)

    fun observeUpdates(source: Source): Flow<Instant>

    enum class Source {
        DETAILS,
        CONTEXT_SHEET,
    }
}

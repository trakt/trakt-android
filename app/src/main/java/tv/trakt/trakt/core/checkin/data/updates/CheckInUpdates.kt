package tv.trakt.trakt.core.checkin.data.updates

import kotlinx.coroutines.flow.Flow
import java.time.Instant

internal interface CheckInUpdates {
    fun notifyUpdate(source: Source)

    fun observeUpdates(): Flow<Pair<Source, Instant?>>

    enum class Source {
        Default,
        HomeUpNext,
        AllHomeUpNext,
        HomeWatchlist,
        AllHomeWatchlist,
        Watchlist,
        MovieContext,
        MovieDetails,
        EpisodeDetails,
    }
}

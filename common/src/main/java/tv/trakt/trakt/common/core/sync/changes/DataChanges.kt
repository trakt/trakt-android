package tv.trakt.trakt.common.core.sync.changes

import kotlinx.coroutines.flow.Flow

/**
 * In-memory signal that user data changed and screens showing it should reload.
 * Publishers say what changed, not who must react.
 */
interface DataChanges {
    fun notify(
        vararg kinds: DataChange.Kind,
        origin: ChangeOrigin? = null,
        targetId: Int? = null,
    )

    fun observe(
        vararg kinds: DataChange.Kind,
        exclude: ChangeOrigin? = null,
    ): Flow<DataChange>
}

package tv.trakt.trakt.widgets.widget.continuewatching

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.widgets.WidgetIntentTarget

@Immutable
internal data class ContinueWatchingWidgetState(
    val items: ImmutableList<ContinueWatchingWidgetItem> = persistentListOf(),
    val error: Boolean = false,
)

@Immutable
internal sealed interface ContinueWatchingWidgetItem {
    val key: String
    val title: String
    val image: Bitmap?
    val progress: Float

    val imageTarget: WidgetIntentTarget
    val titleTarget: WidgetIntentTarget

    @Immutable
    data class Show(
        override val key: String,
        override val title: String,
        override val image: Bitmap?,
        override val progress: Float,
        override val imageTarget: WidgetIntentTarget,
        override val titleTarget: WidgetIntentTarget,
        val episodeId: Int?,
        val episodeText: String,
        val runtimeText: String,
        val remainingEpisodesText: String,
        val loading: Boolean = false,
    ) : ContinueWatchingWidgetItem

    @Immutable
    data class Movie(
        override val key: String,
        override val title: String,
        override val image: Bitmap?,
        override val progress: Float,
        override val imageTarget: WidgetIntentTarget,
        override val titleTarget: WidgetIntentTarget,
        val runtimeText: String,
        val remainingTimeText: String,
    ) : ContinueWatchingWidgetItem
}

/** Marks the item a pending mark-as-watched belongs to, so its footer can swap in a spinner. */
internal fun ContinueWatchingWidgetState.withPendingItem(key: String?): ContinueWatchingWidgetState {
    if (key == null) return this

    return copy(
        items = items
            .map { item ->
                when (item) {
                    is ContinueWatchingWidgetItem.Show -> item.copy(loading = item.key == key)
                    is ContinueWatchingWidgetItem.Movie -> item
                }
            }
            .toImmutableList(),
    )
}

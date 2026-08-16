package tv.trakt.trakt.common.core.klipy.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class GifPage(
    val items: ImmutableList<Gif> = persistentListOf(),
    val page: Int = 1,
    val perPage: Int = 0,
    val hasNext: Boolean = false,
) {
    companion object {
        val Empty = GifPage()
    }
}

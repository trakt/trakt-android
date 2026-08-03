package tv.trakt.trakt.common.model.lists

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.TraktId
import java.time.ZonedDateTime

@Immutable
sealed interface ListsItem {
    val id: TraktId
    val updatedAt: ZonedDateTime

    @Immutable
    data class Custom(
        val list: CustomList,
    ) : ListsItem {
        override val id: TraktId = list.ids.trakt
        override val updatedAt: ZonedDateTime = list.updatedAt
    }

    @Immutable
    data class Smart(
        val list: SmartList,
    ) : ListsItem {
        override val id: TraktId = list.ids.trakt
        override val updatedAt: ZonedDateTime = list.updatedAt
    }
}

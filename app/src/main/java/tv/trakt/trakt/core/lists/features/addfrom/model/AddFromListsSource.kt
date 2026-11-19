package tv.trakt.trakt.core.lists.features.addfrom.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.TraktId

@Immutable
internal sealed interface AddFromListsSource {
    val key: String

    data object Watchlist : AddFromListsSource {
        override val key = "watchlist"
    }

    data class PersonalList(
        val id: TraktId,
        val name: String,
        val count: Int,
    ) : AddFromListsSource {
        override val key = "list:${id.value}"
    }
}

package tv.trakt.trakt.common.model.lists

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.TraktId

@Immutable
data class CustomListMinimal(
    val id: TraktId,
    val ownerId: TraktId,
    val name: String,
    val type: String,
    val itemsCount: Int,
    val displayOrder: Int,
)

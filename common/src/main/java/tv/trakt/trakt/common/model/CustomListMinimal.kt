package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable

@Immutable
data class CustomListMinimal(
    val id: TraktId,
    val ownerId: Int,
    val name: String,
    val type: String,
    val itemsCount: Int,
    val displayOrder: Int,
)

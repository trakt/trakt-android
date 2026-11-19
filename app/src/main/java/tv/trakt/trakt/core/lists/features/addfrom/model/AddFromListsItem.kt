package tv.trakt.trakt.core.lists.features.addfrom.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId

@Immutable
internal data class AddFromListsItem(
    val type: MediaType,
    val id: TraktId,
    val title: String,
    val year: Int?,
    val posterUrl: String?,
) {
    val key: String = toListedKey(type, id)
}

@Immutable
internal data class AddFromListsPick(
    val type: MediaType,
    val id: TraktId,
    val sourceKey: String,
) {
    val key: String = toListedKey(type, id)

    companion object {
        fun from(
            item: AddFromListsItem,
            sourceKey: String,
        ): AddFromListsPick {
            return AddFromListsPick(
                type = item.type,
                id = item.id,
                sourceKey = sourceKey,
            )
        }
    }
}

internal fun toListedKey(
    type: MediaType,
    id: TraktId,
): String = "${type.value}:${id.value}"

internal fun List<AddFromListsPick>.togglePick(pick: AddFromListsPick): List<AddFromListsPick> {
    return when {
        any { it.key == pick.key } -> filter { it.key != pick.key }
        else -> this + pick
    }
}

internal fun List<AddFromListsPick>.toggleAllPicks(candidates: List<AddFromListsPick>): List<AddFromListsPick> {
    val pickedKeys = map { it.key }.toSet()
    val isAllPicked = candidates.all { it.key in pickedKeys }

    if (isAllPicked) {
        val candidateKeys = candidates.map { it.key }.toSet()
        return filter { it.key !in candidateKeys }
    }

    return this + candidates.filter { it.key !in pickedKeys }
}

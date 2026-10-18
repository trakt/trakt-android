package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.serializers.ImmutableListSerializer

@Immutable
@Serializable
data class WhatsNew(
    val id: Int,
    val version: String,
    @Serializable(with = ImmutableListSerializer::class)
    val notes: ImmutableList<WhatsNewNote>,
) {
    @Immutable
    @Serializable
    data class WhatsNewNote(
        val title: String,
        val text: String,
    )
}

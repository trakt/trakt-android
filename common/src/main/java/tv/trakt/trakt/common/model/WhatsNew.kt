package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.serializers.ImmutableListSerializer

@Immutable
@Serializable
data class WhatsNew(
    val id: Int,
    @SerialName("version_name")
    val versionName: String,
    @SerialName("version_code")
    val versionCode: Int,
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

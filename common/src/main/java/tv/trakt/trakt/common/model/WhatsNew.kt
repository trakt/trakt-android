package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class WhatsNew(
    val id: Int,
    @SerialName("version_name")
    val versionName: String,
    @SerialName("version_code")
    val versionCode: Int,
    val notes: List<WhatsNewNote>,
) {
    @Immutable
    @Serializable
    data class WhatsNewNote(
        val title: String,
        val text: String,
    )
}

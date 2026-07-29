package tv.trakt.trakt.common.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.serializers.ImmutableListSerializer
import tv.trakt.trakt.resources.R

@Immutable
@Serializable
data class Sentiments(
    val overall: Overall,
    val analysis: String = "",
    val highlight: String = "",
    @Serializable(with = ImmutableListSerializer::class)
    val pros: ImmutableList<Theme> = EmptyImmutableList,
    @Serializable(with = ImmutableListSerializer::class)
    val cons: ImmutableList<Theme> = EmptyImmutableList,
) {
    val isEmpty: Boolean
        get() = pros.isEmpty() && cons.isEmpty()

    enum class Overall(
        val value: String,
        val displayTextRes: Int,
        val displayIconRes: Int,
    ) {
        Positive("positive", R.string.header_sentiment_positive, R.drawable.ic_thumb_up2_fill),
        Mixed("mixed", R.string.header_sentiment_mixed, R.drawable.ic_thumb_up_down),
        Negative("negative", R.string.header_sentiment_negative, R.drawable.ic_thumb_down2_fill),
        ;

        companion object {
            fun fromValue(value: String?): Overall {
                return entries.firstOrNull { it.value == value } ?: Mixed
            }
        }
    }

    @Immutable
    @Serializable
    data class Theme(
        val value: String,
        val confidence: Float,
    )
}

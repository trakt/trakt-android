package tv.trakt.trakt.common.model

import androidx.annotation.StringRes
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import tv.trakt.trakt.resources.R

@Serializable
enum class PostCreditsScene(
    @param:StringRes val stringRes: Int,
) {
    During(R.string.text_during_credits),
    After(R.string.text_after_credits),
    ;

    companion object {
        fun fromFlags(
            during: Boolean?,
            after: Boolean?,
        ): ImmutableList<PostCreditsScene> {
            return listOfNotNull(
                During.takeIf { during == true },
                After.takeIf { after == true },
            ).toImmutableList()
        }
    }
}

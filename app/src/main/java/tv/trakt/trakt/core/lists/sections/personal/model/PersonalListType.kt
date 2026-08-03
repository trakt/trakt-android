package tv.trakt.trakt.core.lists.sections.personal.model

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.resources.R

@Keep
@Immutable
@Serializable
internal enum class PersonalListType(
    @param:StringRes val displayRes: Int,
    @param:DrawableRes val displayIcon: Int,
) {
    Smart(
        R.string.button_text_smart_lists,
        R.drawable.ic_bolt,
    ),
    Personal(
        R.string.button_text_personal,
        R.drawable.ic_person_trakt,
    ),
    Liked(
        R.string.list_title_liked_lists,
        R.drawable.ic_thumb_up2,
    ),
    Collaborations(
        R.string.list_title_collaborative_lists,
        R.drawable.ic_person_double,
    ),
}

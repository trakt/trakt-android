package tv.trakt.trakt.core.lists.sections.personal.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import tv.trakt.trakt.resources.R

@Immutable
@Serializable
internal enum class PersonalListType(
    @param:StringRes val displayRes: Int,
    @param:DrawableRes val displayIcon: Int,
) {
    Personal(
        R.string.button_text_toggle_lists_personal,
        R.drawable.ic_person_trakt,
    ),
    Liked(
        R.string.button_text_toggle_lists_liked,
        R.drawable.ic_thumb_up2,
    ),
//    Collaborations(
//        R.string.button_text_toggle_lists_collaborations,
//        R.drawable.ic_person_double,
//    ),
}

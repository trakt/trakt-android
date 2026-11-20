package tv.trakt.trakt.common.model.lists

import androidx.annotation.StringRes
import tv.trakt.trakt.resources.R

enum class SmartListPeopleRole(
    val value: String,
    @param:StringRes val displayRes: Int,
) {
    Cast("cast", R.string.option_text_role_cast),
    Directing("directing", R.string.option_text_role_directing),
    Writing("writing", R.string.option_text_role_writing),
    Producing("producing", R.string.option_text_role_producing),
    ;

    companion object {
        fun fromValue(value: String?): SmartListPeopleRole? = entries.firstOrNull { it.value == value }
    }
}

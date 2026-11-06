package tv.trakt.trakt.core.settings.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.resources.R

internal enum class SettingsCategory(
    @param:StringRes val title: Int,
    @param:DrawableRes val icon: Int,
    val settings: List<Int> = EmptyImmutableList,
    val vipSettings: List<Int> = EmptyImmutableList,
) {
    General(
        title = R.string.link_text_general_settings,
        icon = R.drawable.ic_settings,
    ),
    Account(
        title = R.string.link_text_account_settings,
        icon = R.drawable.ic_person,
    ),
    Appearance(
        title = R.string.header_appearance,
        icon = R.drawable.ic_image,
    ),
    Streaming(
        title = R.string.link_text_streaming_sync_settings,
        icon = R.drawable.ic_play,
    ),
    Notifications(
        title = R.string.header_settings_notifications,
        icon = R.drawable.ic_bell,
    ),
    Support(
        title = R.string.link_text_support,
        icon = R.drawable.ic_person_double,
    ),
}

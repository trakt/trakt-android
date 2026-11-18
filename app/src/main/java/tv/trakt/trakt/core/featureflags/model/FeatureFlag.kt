package tv.trakt.trakt.core.featureflags.model

import androidx.annotation.StringRes
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.PREVIEW_FEATURE_PREFIX
import tv.trakt.trakt.resources.R

internal enum class FeatureFlag(
    val id: String,
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
) {
    CharacterPortraits(
        id = "character-portraits",
        title = R.string.preview_feature_title_character_portraits,
        description = R.string.preview_feature_description_character_portraits,
    ),
    ;

    val remoteKey: String
        get() = PREVIEW_FEATURE_PREFIX + id.replace('-', '_')
}

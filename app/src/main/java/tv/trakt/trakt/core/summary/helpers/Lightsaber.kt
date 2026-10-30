package tv.trakt.trakt.core.summary.helpers

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import tv.trakt.trakt.app.BuildConfig
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.STAR_WARS_SABER

internal fun hasLightsaber(title: String): Boolean {
    val enabled = Firebase.remoteConfig.getBoolean(STAR_WARS_SABER) ||
        BuildConfig.FLAVOR == "internal"
    return enabled && title.contains("star wars", ignoreCase = true)
}

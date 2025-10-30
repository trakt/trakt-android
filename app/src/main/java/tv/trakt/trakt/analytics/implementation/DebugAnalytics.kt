package tv.trakt.trakt.analytics.implementation

import timber.log.Timber
import tv.trakt.trakt.analytics.Analytics

internal class DebugAnalytics : Analytics {
    override fun logScreenView(
        screenName: String,
        screenClass: String,
    ) {
        Timber.d("logScreenView: screenName=$screenName")
    }

    override fun logUserLogin() {
        Timber.d("logUserLogin")
    }

    override fun logUserLogout() {
        Timber.d("logUserLogout")
    }
}

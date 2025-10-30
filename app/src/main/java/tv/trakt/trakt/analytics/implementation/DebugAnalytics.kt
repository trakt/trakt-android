package tv.trakt.trakt.analytics.implementation

import timber.log.Timber
import tv.trakt.trakt.analytics.Analytics

internal class DebugAnalytics(
    override val reactions: Analytics.Reactions,
) : Analytics {
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

internal class DebugAnalyticsReactions : Analytics.Reactions {
    override fun logReactionAdd(
        reaction: String,
        source: String,
    ) {
        Timber.d("logReactionAdd: reaction=$reaction, source=$source")
    }

    override fun logReactionRemove(source: String) {
        Timber.d("logReactionRemove: source=$source")
    }
}

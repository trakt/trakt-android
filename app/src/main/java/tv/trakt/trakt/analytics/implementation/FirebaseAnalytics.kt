package tv.trakt.trakt.analytics.implementation

import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.Event.LOGIN
import com.google.firebase.analytics.FirebaseAnalytics.Event.SCREEN_VIEW
import com.google.firebase.analytics.FirebaseAnalytics.Param
import tv.trakt.trakt.analytics.Analytics

// Android V3 events identifier
private const val EVENT_NAME_PREFIX = "av3_"

private fun eventName(name: String) = "$EVENT_NAME_PREFIX$name"

internal class FirebaseAnalytics(
    private val firebase: FirebaseAnalytics,
    override val reactions: Analytics.Reactions,
) : Analytics {
    companion object Event {
        const val LOGOUT = "logout"
    }

    override fun logScreenView(
        screenName: String,
        screenClass: String,
    ) {
        firebase.logEvent(
            eventName(SCREEN_VIEW),
            bundleOf(
                Param.SCREEN_NAME to screenName,
                Param.SCREEN_CLASS to screenClass,
            ),
        )
    }

    override fun logUserLogin() {
        firebase.logEvent(
            eventName(LOGIN),
            null,
        )
    }

    override fun logUserLogout() {
        firebase.logEvent(
            eventName(LOGOUT),
            null,
        )
    }
}

internal class FirebaseAnalyticsReactions(
    private val firebase: FirebaseAnalytics,
) : Analytics.Reactions {
    companion object Event {
        const val REACTIONS_ADD = "reactions_comment_add"
        const val REACTIONS_REMOVE = "reactions_comment_remove"
    }

    override fun logReactionAdd(
        reaction: String,
        source: String,
    ) {
        firebase.logEvent(
            eventName(REACTIONS_ADD),
            bundleOf(
                "reaction" to reaction.lowercase(),
                "source" to source.lowercase(),
            ),
        )
    }

    override fun logReactionRemove(source: String) {
        firebase.logEvent(
            eventName(REACTIONS_REMOVE),
            bundleOf(
                "source" to source.lowercase(),
            ),
        )
    }
}

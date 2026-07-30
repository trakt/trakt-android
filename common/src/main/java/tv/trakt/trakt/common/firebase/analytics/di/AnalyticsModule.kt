package tv.trakt.trakt.common.firebase.analytics.di

import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.crashlytics
import org.koin.dsl.module
import tv.trakt.trakt.common.BuildConfig
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.firebase.analytics.implementation.DebugAnalytics
import tv.trakt.trakt.common.firebase.analytics.implementation.DebugAnalyticsComments
import tv.trakt.trakt.common.firebase.analytics.implementation.DebugAnalyticsPlayback
import tv.trakt.trakt.common.firebase.analytics.implementation.DebugAnalyticsProgress
import tv.trakt.trakt.common.firebase.analytics.implementation.DebugAnalyticsRatings
import tv.trakt.trakt.common.firebase.analytics.implementation.DebugAnalyticsReactions
import tv.trakt.trakt.common.firebase.analytics.implementation.DebugAnalyticsTrivia
import tv.trakt.trakt.common.firebase.analytics.implementation.FirebaseAnalytics
import tv.trakt.trakt.common.firebase.analytics.implementation.FirebaseAnalyticsComments
import tv.trakt.trakt.common.firebase.analytics.implementation.FirebaseAnalyticsPlayback
import tv.trakt.trakt.common.firebase.analytics.implementation.FirebaseAnalyticsProgress
import tv.trakt.trakt.common.firebase.analytics.implementation.FirebaseAnalyticsRatings
import tv.trakt.trakt.common.firebase.analytics.implementation.FirebaseAnalyticsReactions
import tv.trakt.trakt.common.firebase.analytics.implementation.FirebaseAnalyticsTrivia

val analyticsModule = module {
    single<Analytics> {
        if (BuildConfig.DEBUG) {
            DebugAnalytics(
                reactions = DebugAnalyticsReactions(),
                ratings = DebugAnalyticsRatings(),
                comments = DebugAnalyticsComments(),
                progress = DebugAnalyticsProgress(),
                trivia = DebugAnalyticsTrivia(),
                playback = DebugAnalyticsPlayback(),
            )
        } else {
            val analytics = Firebase.analytics
            val crashlytics = Firebase.crashlytics
            FirebaseAnalytics(
                firebaseAnalytics = analytics,
                firebaseCrashlytics = crashlytics,
                reactions = FirebaseAnalyticsReactions(analytics),
                ratings = FirebaseAnalyticsRatings(analytics),
                comments = FirebaseAnalyticsComments(analytics),
                progress = FirebaseAnalyticsProgress(analytics),
                trivia = FirebaseAnalyticsTrivia(analytics),
                playback = FirebaseAnalyticsPlayback(analytics),
            )
        }
    }
}

package tv.trakt.trakt.common.firebase.analytics.di

import android.content.Context
import android.os.Build
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.crashlytics
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.scope.Scope
import org.koin.dsl.module
import tv.trakt.trakt.common.BuildConfig
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.firebase.analytics.implementation.CompositeAnalytics
import tv.trakt.trakt.common.firebase.analytics.implementation.debug.DebugAnalytics
import tv.trakt.trakt.common.firebase.analytics.implementation.debug.DebugAnalyticsComments
import tv.trakt.trakt.common.firebase.analytics.implementation.debug.DebugAnalyticsLists
import tv.trakt.trakt.common.firebase.analytics.implementation.debug.DebugAnalyticsPlayback
import tv.trakt.trakt.common.firebase.analytics.implementation.debug.DebugAnalyticsProgress
import tv.trakt.trakt.common.firebase.analytics.implementation.debug.DebugAnalyticsRatings
import tv.trakt.trakt.common.firebase.analytics.implementation.debug.DebugAnalyticsReactions
import tv.trakt.trakt.common.firebase.analytics.implementation.debug.DebugAnalyticsToday
import tv.trakt.trakt.common.firebase.analytics.implementation.debug.DebugAnalyticsTrivia
import tv.trakt.trakt.common.firebase.analytics.implementation.firebase.FirebaseAnalytics
import tv.trakt.trakt.common.firebase.analytics.implementation.firebase.FirebaseAnalyticsComments
import tv.trakt.trakt.common.firebase.analytics.implementation.firebase.FirebaseAnalyticsLists
import tv.trakt.trakt.common.firebase.analytics.implementation.firebase.FirebaseAnalyticsPlayback
import tv.trakt.trakt.common.firebase.analytics.implementation.firebase.FirebaseAnalyticsProgress
import tv.trakt.trakt.common.firebase.analytics.implementation.firebase.FirebaseAnalyticsRatings
import tv.trakt.trakt.common.firebase.analytics.implementation.firebase.FirebaseAnalyticsReactions
import tv.trakt.trakt.common.firebase.analytics.implementation.firebase.FirebaseAnalyticsToday
import tv.trakt.trakt.common.firebase.analytics.implementation.firebase.FirebaseAnalyticsTrivia
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalAnalytics
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalAnalyticsComments
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalAnalyticsLists
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalAnalyticsPlayback
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalAnalyticsProgress
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalAnalyticsRatings
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalAnalyticsReactions
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalAnalyticsToday
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalAnalyticsTrivia
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.HalEngine
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.data.HalEventStore
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.data.remote.HalDeviceKind
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.data.remote.HalSender
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.data.remote.KtorHalSender
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.data.remote.halUserAgent
import tv.trakt.trakt.common.firebase.analytics.implementation.hal.data.work.HalSendWorker
import tv.trakt.trakt.common.helpers.extensions.isTelevision
import kotlin.time.Duration.Companion.seconds

private const val HAL_ENDPOINT = "https://hal.trakt.tv/e"
private const val TABLET_MIN_WIDTH_DP = 600

private fun Context.halDeviceKind(): HalDeviceKind {
    return when {
        isTelevision() -> HalDeviceKind.Television
        resources.configuration.smallestScreenWidthDp >= TABLET_MIN_WIDTH_DP -> HalDeviceKind.Tablet
        else -> HalDeviceKind.Phone
    }
}

val analyticsModule = module {
    singleOf(::HalEventStore)

    single<HalSender> {
        val client = HttpClient(get<HttpClientEngine>()) {
            install(HttpTimeout) {
                val timeoutMillis = 10.seconds.inWholeMilliseconds
                requestTimeoutMillis = timeoutMillis
                socketTimeoutMillis = timeoutMillis
                connectTimeoutMillis = timeoutMillis
            }
        }
        KtorHalSender(
            client = client,
            endpoint = HAL_ENDPOINT,
            apiKey = BuildConfig.TRAKT_API_KEY,
            userAgent = halUserAgent(
                appVersion = BuildConfig.VERSION_NAME,
                androidRelease = Build.VERSION.RELEASE,
                deviceKind = androidContext().halDeviceKind(),
            ),
        )
    }

    workerOf(::HalSendWorker)

    single<Analytics> {
        if (BuildConfig.DEBUG) {
            DebugAnalytics(
                reactions = DebugAnalyticsReactions(),
                ratings = DebugAnalyticsRatings(),
                comments = DebugAnalyticsComments(),
                progress = DebugAnalyticsProgress(),
                trivia = DebugAnalyticsTrivia(),
                playback = DebugAnalyticsPlayback(),
                lists = DebugAnalyticsLists(),
                today = DebugAnalyticsToday(),
            )
        } else {
            CompositeAnalytics(
                targets = listOf(
                    firebaseAnalytics(),
                    halAnalytics(),
                ),
            )
        }
    }
}

private fun firebaseAnalytics(): Analytics {
    val analytics = Firebase.analytics
    return FirebaseAnalytics(
        firebaseAnalytics = analytics,
        firebaseCrashlytics = Firebase.crashlytics,
        reactions = FirebaseAnalyticsReactions(analytics),
        ratings = FirebaseAnalyticsRatings(analytics),
        comments = FirebaseAnalyticsComments(analytics),
        progress = FirebaseAnalyticsProgress(analytics),
        trivia = FirebaseAnalyticsTrivia(analytics),
        playback = FirebaseAnalyticsPlayback(analytics),
        lists = FirebaseAnalyticsLists(analytics),
        today = FirebaseAnalyticsToday(analytics),
    )
}

private fun Scope.halAnalytics(): Analytics {
    val context = androidContext()
    val engine = HalEngine(
        store = get(),
        scheduleSend = { HalSendWorker.schedule(context) },
    )
    return HalAnalytics(
        events = engine,
        reactions = HalAnalyticsReactions(engine),
        ratings = HalAnalyticsRatings(engine),
        comments = HalAnalyticsComments(engine),
        progress = HalAnalyticsProgress(engine),
        trivia = HalAnalyticsTrivia(),
        playback = HalAnalyticsPlayback(),
        lists = HalAnalyticsLists(engine),
        today = HalAnalyticsToday(engine),
    )
}

package tv.trakt.trakt.common.firebase.analytics.di

import android.content.Context
import android.os.Build
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.koin.androidContext
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
import tv.trakt.trakt.common.firebase.analytics.implementation.HalAnalytics
import tv.trakt.trakt.common.firebase.analytics.implementation.HalAnalyticsComments
import tv.trakt.trakt.common.firebase.analytics.implementation.HalAnalyticsPlayback
import tv.trakt.trakt.common.firebase.analytics.implementation.HalAnalyticsProgress
import tv.trakt.trakt.common.firebase.analytics.implementation.HalAnalyticsRatings
import tv.trakt.trakt.common.firebase.analytics.implementation.HalAnalyticsReactions
import tv.trakt.trakt.common.firebase.analytics.implementation.HalAnalyticsTrivia
import tv.trakt.trakt.common.firebase.analytics.implementation.HalDeviceKind
import tv.trakt.trakt.common.firebase.analytics.implementation.HalEngine
import tv.trakt.trakt.common.firebase.analytics.implementation.KtorHalSender
import tv.trakt.trakt.common.firebase.analytics.implementation.halUserAgent
import tv.trakt.trakt.common.helpers.extensions.isTelevision
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider
import tv.trakt.trakt.common.helpers.lifecycle.AppLifecycleProvider.State.BACKGROUND
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
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            val client = HttpClient(get<HttpClientEngine>()) {
                install(HttpTimeout) {
                    val timeoutMillis = 10.seconds.inWholeMilliseconds
                    requestTimeoutMillis = timeoutMillis
                    socketTimeoutMillis = timeoutMillis
                    connectTimeoutMillis = timeoutMillis
                }
            }
            val engine = HalEngine(
                sender = KtorHalSender(
                    client = client,
                    endpoint = HAL_ENDPOINT,
                    apiKey = BuildConfig.TRAKT_API_KEY,
                    userAgent = halUserAgent(
                        appVersion = BuildConfig.VERSION_NAME,
                        androidRelease = Build.VERSION.RELEASE,
                        deviceKind = androidContext().halDeviceKind(),
                    ),
                ),
                scope = scope,
            )

            get<AppLifecycleProvider>()
                .observeState(BACKGROUND)
                .onEach { engine.flush() }
                .launchIn(scope)

            val crashlytics = Firebase.crashlytics
            HalAnalytics(
                events = engine,
                setCrashlyticsUserId = crashlytics::setUserId,
                reactions = HalAnalyticsReactions(engine),
                ratings = HalAnalyticsRatings(engine),
                comments = HalAnalyticsComments(engine),
                progress = HalAnalyticsProgress(engine),
                trivia = HalAnalyticsTrivia(),
                playback = HalAnalyticsPlayback(),
            )
        }
    }
}

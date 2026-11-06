package tv.trakt.trakt.core.summary.soundtrack.ui

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import timber.log.Timber
import java.net.URISyntaxException

private const val BASE_URL = "https://app.trakt.tv/"
private const val BRIDGE_NAME = "TraktSpotify"

/**
 * Plays Spotify tracks in place through Spotify's iFrame API. One embed is created for the
 * first track and reused for every later one. A plain embed would need a second tap inside it.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun SpotifyEmbedPlayer(
    spotifyId: String,
    playToken: Int,
    onPlayingChange: (Boolean) -> Unit,
    embedHeight: Dp,
    modifier: Modifier = Modifier,
) {
    val currentOnPlayingChange by rememberUpdatedState(onPlayingChange)
    val initialUri = remember { spotifyUri(spotifyId) }
    var requestedToken by remember { mutableIntStateOf(playToken) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(webView, playToken) {
        val view = webView ?: return@LaunchedEffect
        if (playToken == requestedToken) return@LaunchedEffect

        requestedToken = playToken
        view.evaluateJavascript("playTrack('${spotifyUri(spotifyId)}');", null)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, webView) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                webView?.evaluateJavascript("pausePlayback();", null)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(Color.TRANSPARENT)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                settings.mediaPlaybackRequiresUserGesture = false
                webViewClient = ExternalLinksWebViewClient()
                addJavascriptInterface(
                    SpotifyBridge { playing -> currentOnPlayingChange(playing) },
                    BRIDGE_NAME,
                )
                loadDataWithBaseURL(BASE_URL, embedHtml(initialUri, embedHeight), "text/html", "utf-8", null)
                webView = this
            }
        },
        onRelease = { view ->
            view.removeJavascriptInterface(BRIDGE_NAME)
            view.destroy()
        },
    )
}

/**
 * Keeps the embed in place. Links the card opens ("Get Spotify", track title, `target=_blank`)
 * navigate the main frame or use app schemes, so they go to the system instead of the WebView.
 */
private class ExternalLinksWebViewClient : WebViewClient() {
    override fun shouldOverrideUrlLoading(
        view: WebView,
        request: WebResourceRequest,
    ): Boolean {
        val isWebUrl = request.url.scheme in setOf("http", "https")
        if (!request.isForMainFrame && isWebUrl) {
            return false
        }

        openExternally(view.context, request.url)
        return true
    }

    private fun openExternally(
        context: Context,
        uri: Uri,
    ) {
        try {
            val intent = when (uri.scheme) {
                "intent" -> Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                    component = null
                    selector = null
                }
                else -> Intent(Intent.ACTION_VIEW, uri)
            }
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (error: ActivityNotFoundException) {
            Timber.w(error, "No app to open Spotify embed link")
        } catch (error: URISyntaxException) {
            Timber.w(error, "Invalid Spotify embed link")
        }
    }
}

private class SpotifyBridge(
    private val onPlayingChange: (Boolean) -> Unit,
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onPlaybackUpdate(isPaused: Boolean) {
        mainHandler.post { onPlayingChange(!isPaused) }
    }
}

private fun spotifyUri(spotifyId: String): String {
    return "spotify:track:${spotifyId.filter(Char::isLetterOrDigit)}"
}

private fun embedHtml(
    initialUri: String,
    embedHeight: Dp,
): String {
    return """
        <!doctype html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <style>html, body { margin: 0; padding: 0; background: transparent; overflow: hidden; }</style>
        </head>
        <body>
          <div id="embed"></div>
          <script>
            var controller = null;
            var loadedUri = '$initialUri';
            window.onSpotifyIframeApiReady = function (api) {
              var options = { uri: loadedUri, width: '100%', height: '${embedHeight.value.toInt()}' };
              api.createController(document.getElementById('embed'), options, function (created) {
                controller = created;
                created.addListener('ready', function () { created.play(); });
                created.addListener('playback_update', function (event) {
                  $BRIDGE_NAME.onPlaybackUpdate(event.data.isPaused);
                });
              });
            };
            function playTrack(uri) {
              if (controller == null) { loadedUri = uri; return; }
              if (uri === loadedUri) { controller.togglePlay(); return; }
              loadedUri = uri;
              controller.loadUri(uri);
              controller.play();
            }
            function pausePlayback() {
              if (controller != null) { controller.pause(); }
            }
          </script>
          <script src="https://open.spotify.com/embed/iframe-api/v1" async></script>
        </body>
        </html>
        """.trimIndent()
}

package tv.trakt.trakt.app.core.player.plex

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import tv.trakt.trakt.app.ui.theme.TraktTheme

private const val EXTRA_PLEX_VIDEO_URL = "extra_plex_video_url"
private const val EXTRA_PLEX_VIDEO_TITLE = "extra_plex_video_title"
private const val EXTRA_PLEX_VIDEO_SUBTITLE = "extra_plex_video_subtitle"

class TvPlexPlayerActivity : ComponentActivity() {
    companion object {
        fun createIntent(
            context: Context,
            videoUrl: String,
            videoTitle: String,
            videoSubtitle: String?,
        ): Intent {
            return Intent(context, TvPlexPlayerActivity::class.java).apply {
                putExtra(EXTRA_PLEX_VIDEO_URL, videoUrl)
                putExtra(EXTRA_PLEX_VIDEO_TITLE, videoTitle)
                putExtra(EXTRA_PLEX_VIDEO_SUBTITLE, videoSubtitle)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val videoUrl = intent.getStringExtra(EXTRA_PLEX_VIDEO_URL)
        val videoTitle = intent.getStringExtra(EXTRA_PLEX_VIDEO_TITLE) ?: ""
        val videoSubtitle = intent.getStringExtra(EXTRA_PLEX_VIDEO_SUBTITLE)

        if (videoUrl.isNullOrEmpty()) {
            // No video URL, finish activity
            finish()
            return
        }

        setContent {
            TraktTheme {
                TvPlexPlayerScreen(
                    videoUrl = videoUrl,
                    videoTitle = videoTitle,
                    videoSubtitle = videoSubtitle,
                )
            }
        }
    }
}

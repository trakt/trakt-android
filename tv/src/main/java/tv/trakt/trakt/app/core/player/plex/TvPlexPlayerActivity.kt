package tv.trakt.trakt.app.core.player.plex

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import tv.trakt.trakt.app.ui.theme.TraktTheme
import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.toTraktId

private const val EXTRA_PLEX_VIDEO_URL = "extra_plex_video_url"
private const val EXTRA_PLEX_VIDEO_SECONDARY_URLS = "extra_plex_video_secondary_urls"
private const val EXTRA_PLEX_VIDEO_TITLE = "extra_plex_video_title"
private const val EXTRA_PLEX_VIDEO_SUBTITLE = "extra_plex_video_subtitle"
private const val EXTRA_PLEX_VIDEO_PROGRESS = "extra_plex_video_progress"
private const val EXTRA_PLEX_MEDIA_ID = "extra_plex_media_id"
private const val EXTRA_PLEX_MEDIA_TYPE = "extra_plex_media_type"

class TvPlexPlayerActivity : ComponentActivity() {
    companion object {
        fun createIntent(
            context: Context,
            mediaId: TraktId,
            mediaType: MediaType,
            primaryVideoUrl: String,
            secondaryVideoUrls: List<String>,
            videoTitle: String,
            videoSubtitle: String?,
            videoProgress: Float?,
        ): Intent {
            return Intent(context, TvPlexPlayerActivity::class.java).apply {
                putExtra(EXTRA_PLEX_MEDIA_ID, mediaId.value)
                putExtra(EXTRA_PLEX_MEDIA_TYPE, mediaType.name)
                putExtra(EXTRA_PLEX_VIDEO_URL, primaryVideoUrl)
                putExtra(EXTRA_PLEX_VIDEO_SECONDARY_URLS, secondaryVideoUrls.toTypedArray())
                putExtra(EXTRA_PLEX_VIDEO_TITLE, videoTitle)
                putExtra(EXTRA_PLEX_VIDEO_SUBTITLE, videoSubtitle)
                videoProgress?.let {
                    putExtra(EXTRA_PLEX_VIDEO_PROGRESS, it.coerceIn(0F..100F))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mediaId = intent.getIntExtra(EXTRA_PLEX_MEDIA_ID, -1).toTraktId()
        val mediaType = intent.getStringExtra(EXTRA_PLEX_MEDIA_TYPE)?.let { MediaType.valueOf(it) }

        val videoUrl = intent.getStringExtra(EXTRA_PLEX_VIDEO_URL)
        val videoSecondaryUrls = intent.getStringArrayExtra(EXTRA_PLEX_VIDEO_SECONDARY_URLS)?.toList().orEmpty()
        val videoTitle = intent.getStringExtra(EXTRA_PLEX_VIDEO_TITLE) ?: ""
        val videoSubtitle = intent.getStringExtra(EXTRA_PLEX_VIDEO_SUBTITLE)
        val videoProgress = intent.getFloatExtra(EXTRA_PLEX_VIDEO_PROGRESS, 0f)

        if (videoUrl.isNullOrEmpty()) {
            // No video URL, finish activity
            finish()
            return
        }

        if (mediaType == null) {
            finish()
            return
        }

        setContent {
            TraktTheme {
                TvPlexPlayerScreen(
                    videoUrl = videoUrl,
                    secondaryVideoUrls = videoSecondaryUrls,
                    videoTitle = videoTitle,
                    videoSubtitle = videoSubtitle,
                    videoProgress = videoProgress,
                    mediaId = mediaId,
                    mediaType = mediaType,
                )
            }
        }
    }
}

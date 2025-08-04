package tv.trakt.trakt.app.core.player

import androidx.navigation.NavController

internal fun NavController.navigateToPlayer(videoUrl: String) {
    with(context) {
        val intent = TvPlayerActivity.createIntent(this, videoUrl)
        startActivity(intent)
    }
}

package tv.trakt.trakt.core.settings.categories.streaming

import androidx.lifecycle.ViewModel
import tv.trakt.trakt.common.firebase.analytics.Analytics

internal class StreamingSettingsViewModel(
    analytics: Analytics,
) : ViewModel() {
    init {
        analytics.logScreenView(
            screenName = "settings_streaming",
        )
    }
}

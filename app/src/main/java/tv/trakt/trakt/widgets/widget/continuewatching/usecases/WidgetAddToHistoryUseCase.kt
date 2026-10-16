package tv.trakt.trakt.widgets.widget.continuewatching.usecases

import timber.log.Timber
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase

internal class WidgetAddToHistoryUseCase(
    private val updateHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val dataChanges: DataChanges,
    private val analytics: Analytics,
) {
    suspend fun addToHistory(episodeId: TraktId) {
        updateHistoryUseCase.addToHistory(episodeId = episodeId)

        analytics.progress.logAddWatchedMedia(
            mediaType = "episode",
            source = "widget_up_next",
            date = null,
        )

        dataChanges.notify(Kind.History)

        loadUserProgress()
    }

    /** Progress powers the app's own lists; a failure here must not undo the recorded play. */
    private suspend fun loadUserProgress() {
        try {
            loadUserProgressUseCase.loadShowsProgress()
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.w(error, "Failed to refresh shows progress after a widget history add")
            }
        }
    }
}

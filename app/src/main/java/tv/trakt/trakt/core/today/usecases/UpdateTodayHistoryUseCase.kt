package tv.trakt.trakt.core.today.usecases

import tv.trakt.trakt.common.core.user.data.local.UserProgressLocalDataSource
import tv.trakt.trakt.common.core.user.data.local.watchlist.UserWatchlistLocalDataSource
import tv.trakt.trakt.common.core.user.data.local.watchlist.WatchlistUpdates
import tv.trakt.trakt.common.core.user.data.local.watchlist.WatchlistUpdates.Source.Default
import tv.trakt.trakt.common.core.user.data.local.watchlist.minimal.UserWatchlistMinimalLocalDataSource
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.core.user.usecases.progress.updates.ProgressUpdates
import tv.trakt.trakt.core.home.sections.upnext.features.all.data.local.UpNextUpdates
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowHistoryUseCase
import tv.trakt.trakt.core.today.data.local.TodayLocalDataSource
import tv.trakt.trakt.core.today.model.TodayForYouItem
import tv.trakt.trakt.core.today.model.TodayMedia

internal class UpdateTodayHistoryUseCase(
    private val updateEpisodeHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val updateMovieHistoryUseCase: UpdateMovieHistoryUseCase,
    private val updateShowHistoryUseCase: UpdateShowHistoryUseCase,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val userProgressLocalSource: UserProgressLocalDataSource,
    private val progressUpdates: ProgressUpdates,
    private val userWatchlistLocalSource: UserWatchlistLocalDataSource,
    private val userWatchlistMinLocalSource: UserWatchlistMinimalLocalDataSource,
    private val watchlistUpdates: WatchlistUpdates,
    private val upNextUpdates: UpNextUpdates,
    private val todayLocalSource: TodayLocalDataSource,
) {
    suspend fun addToWatched(item: TodayForYouItem) {
        when (item) {
            is TodayForYouItem.UpNext -> {
                updateEpisodeHistoryUseCase.addToHistory(episodeId = item.episode.ids.trakt)
                loadProgressUseCase.loadShowsProgress()
            }
            is TodayForYouItem.StartWatching -> {
                addMediaToWatched(item.media)
            }
        }

        todayLocalSource.setForYouWatched(key = item.key, watched = true)
        notifyHistoryUpdate()
    }

    suspend fun removeFromWatched(item: TodayForYouItem) {
        when (item) {
            is TodayForYouItem.UpNext -> {
                updateEpisodeHistoryUseCase.removeEpisodeFromHistory(item.episode.ids.trakt.value)
                loadProgressUseCase.loadShowsProgress()
            }
            is TodayForYouItem.StartWatching -> {
                removeMediaFromWatched(item.media)
            }
        }

        todayLocalSource.setForYouWatched(key = item.key, watched = false)
        notifyHistoryUpdate()
    }

    private fun notifyHistoryUpdate() {
        upNextUpdates.notifyUpdate(UpNextUpdates.Source.Today)
    }

    private suspend fun addMediaToWatched(media: TodayMedia) {
        val id = media.ids.trakt

        when (media) {
            is TodayMedia.MovieMedia -> {
                updateMovieHistoryUseCase.addToWatched(movieId = id)
                loadProgressUseCase.loadMoviesProgress()
                userWatchlistLocalSource.removeMovies(ids = setOf(id))
                userWatchlistMinLocalSource.removeMovies(ids = setOf(id))
            }
            is TodayMedia.ShowMedia -> {
                updateShowHistoryUseCase.addToWatched(showId = id)
                loadProgressUseCase.loadShowsProgress()
                userWatchlistLocalSource.removeShows(ids = setOf(id))
                userWatchlistMinLocalSource.removeShows(ids = setOf(id))
            }
        }

        watchlistUpdates.notifyUpdate(Default)
    }

    private suspend fun removeMediaFromWatched(media: TodayMedia) {
        val id = media.ids.trakt

        when (media) {
            is TodayMedia.MovieMedia -> {
                updateMovieHistoryUseCase.removeAllFromHistory(id)
                userProgressLocalSource.removeMovies(ids = setOf(id))
            }
            is TodayMedia.ShowMedia -> {
                updateShowHistoryUseCase.removeAllFromHistory(id)
                userProgressLocalSource.removeShows(ids = setOf(id))
            }
        }

        progressUpdates.notifyUpdate()
    }
}

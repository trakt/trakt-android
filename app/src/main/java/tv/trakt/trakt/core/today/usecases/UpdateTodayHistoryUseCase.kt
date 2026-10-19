package tv.trakt.trakt.core.today.usecases

import tv.trakt.trakt.common.core.sync.changes.ChangeOrigin
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.core.today.model.TodayMedia
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.data.local.UserProgressLocalSource
import tv.trakt.trakt.common.data.local.UserWatchlistLocalSource
import tv.trakt.trakt.common.data.local.UserWatchlistMinimalLocalSource
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowHistoryUseCase
import tv.trakt.trakt.core.today.data.local.TodayLocalSource
import tv.trakt.trakt.core.today.model.TodayForYouItem

internal class UpdateTodayHistoryUseCase(
    private val updateEpisodeHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val updateMovieHistoryUseCase: UpdateMovieHistoryUseCase,
    private val updateShowHistoryUseCase: UpdateShowHistoryUseCase,
    private val loadProgressUseCase: LoadUserProgressUseCase,
    private val userProgressLocalSource: UserProgressLocalSource,
    private val dataChanges: DataChanges,
    private val userWatchlistLocalSource: UserWatchlistLocalSource,
    private val userWatchlistMinLocalSource: UserWatchlistMinimalLocalSource,
    private val todayLocalSource: TodayLocalSource,
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
        dataChanges.notify(Kind.History, origin = ChangeOrigin.TodayStories)
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

        dataChanges.notify(Kind.Watchlist)
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

        dataChanges.notify(Kind.Progress)
    }
}

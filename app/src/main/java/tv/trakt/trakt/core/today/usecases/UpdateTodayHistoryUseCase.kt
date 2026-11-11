package tv.trakt.trakt.core.today.usecases

import tv.trakt.trakt.common.core.sync.changes.ChangeOrigin
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.core.today.model.TodayForYouItem
import tv.trakt.trakt.common.core.today.model.TodayMedia
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateEpisodeHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateMovieHistoryUseCase
import tv.trakt.trakt.core.sync.usecases.UpdateShowHistoryUseCase
import tv.trakt.trakt.core.today.data.local.TodayLocalSource

internal class UpdateTodayHistoryUseCase(
    private val updateEpisodeHistoryUseCase: UpdateEpisodeHistoryUseCase,
    private val updateMovieHistoryUseCase: UpdateMovieHistoryUseCase,
    private val updateShowHistoryUseCase: UpdateShowHistoryUseCase,
    private val todayLocalSource: TodayLocalSource,
) {
    suspend fun addToWatched(item: TodayForYouItem) {
        when (item) {
            is TodayForYouItem.UpNext -> {
                updateEpisodeHistoryUseCase.addToHistory(
                    episodeId = item.episode.ids.trakt,
                    origin = ChangeOrigin.TodayStories,
                )
            }
            is TodayForYouItem.StartWatching -> {
                addMediaToWatched(item.media)
            }
        }

        todayLocalSource.setForYouWatched(key = item.key, watched = true)
    }

    suspend fun removeFromWatched(item: TodayForYouItem) {
        when (item) {
            is TodayForYouItem.UpNext -> {
                updateEpisodeHistoryUseCase.removeEpisodeFromHistory(
                    episodeId = item.episode.ids.trakt.value,
                    origin = ChangeOrigin.TodayStories,
                )
            }
            is TodayForYouItem.StartWatching -> {
                removeMediaFromWatched(item.media)
            }
        }

        todayLocalSource.setForYouWatched(key = item.key, watched = false)
    }

    private suspend fun addMediaToWatched(media: TodayMedia) {
        val id = media.ids.trakt

        when (media) {
            is TodayMedia.MovieMedia -> {
                updateMovieHistoryUseCase.addToWatched(
                    movieId = id,
                    origin = ChangeOrigin.TodayStories,
                )
            }
            is TodayMedia.ShowMedia -> {
                updateShowHistoryUseCase.addToWatched(
                    showId = id,
                    origin = ChangeOrigin.TodayStories,
                )
            }
        }
    }

    private suspend fun removeMediaFromWatched(media: TodayMedia) {
        val id = media.ids.trakt

        when (media) {
            is TodayMedia.MovieMedia -> {
                updateMovieHistoryUseCase.removeAllFromHistory(
                    movieId = id,
                    origin = ChangeOrigin.TodayStories,
                )
            }
            is TodayMedia.ShowMedia -> {
                updateShowHistoryUseCase.removeAllFromHistory(
                    showId = id,
                    origin = ChangeOrigin.TodayStories,
                )
            }
        }
    }
}

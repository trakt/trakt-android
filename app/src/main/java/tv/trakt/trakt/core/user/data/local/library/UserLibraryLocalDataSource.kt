package tv.trakt.trakt.core.user.data.local.library

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.library.model.LibraryItem

internal interface UserLibraryLocalDataSource {
    suspend fun setMovies(movies: List<LibraryItem.MovieItem>)

    suspend fun getMovies(): List<LibraryItem.MovieItem>

    suspend fun removeMovies(ids: Set<TraktId>)

    suspend fun isMoviesLoaded(): Boolean

    suspend fun setEpisodes(episodes: List<LibraryItem.EpisodeItem>)

    suspend fun getEpisodes(): List<LibraryItem.EpisodeItem>

    suspend fun isEpisodesLoaded(): Boolean

    suspend fun removeEpisodes(ids: Set<TraktId>)

    suspend fun getAll(): List<LibraryItem>

    fun clear()
}

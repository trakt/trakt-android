package tv.trakt.trakt.core.discover.sections.releases.data.local.movies

import tv.trakt.trakt.core.calendar.model.CalendarItem

internal interface ReleasesMoviesLocalDataSource {
    suspend fun setMovies(movies: List<CalendarItem.MovieItem>)

    suspend fun getMovies(): List<CalendarItem.MovieItem>

    suspend fun clear()
}

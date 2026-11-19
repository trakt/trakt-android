package tv.trakt.trakt.common.core.translations.model

import tv.trakt.trakt.common.core.lists.model.WatchlistItem
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.discover.DiscoverItem
import tv.trakt.trakt.common.model.recommended.RecommendedItem
import tv.trakt.trakt.common.networking.api.v3.model.V3BulkIntlResponse

data class LocalizedTitles(
    val movies: Map<TraktId, String> = emptyMap(),
    val shows: Map<TraktId, String> = emptyMap(),
    val episodes: Map<TraktId, String> = emptyMap(),
) {
    fun movie(movie: Movie): Movie {
        val title = movies[movie.ids.trakt] ?: return movie
        return movie.copy(title = title)
    }

    fun show(show: Show): Show {
        val title = shows[show.ids.trakt] ?: return show
        return show.copy(title = title)
    }

    fun episode(episode: Episode): Episode {
        val title = episodes[episode.ids.trakt] ?: return episode
        return episode.copy(title = title)
    }

    fun discoverItem(item: DiscoverItem): DiscoverItem {
        return when (item) {
            is DiscoverItem.MovieItem -> item.copy(movie = movie(item.movie))
            is DiscoverItem.ShowItem -> item.copy(show = show(item.show))
        }
    }

    fun recommendedItem(item: RecommendedItem): RecommendedItem {
        return when (item) {
            is RecommendedItem.MovieItem -> item.copy(movie = movie(item.movie))
            is RecommendedItem.ShowItem -> item.copy(show = show(item.show))
        }
    }

    fun watchlistItem(item: WatchlistItem): WatchlistItem {
        return when (item) {
            is WatchlistItem.MovieItem -> item.copy(movie = movie(item.movie))
            is WatchlistItem.ShowItem -> item.copy(show = show(item.show))
        }
    }

    companion object {
        val Empty = LocalizedTitles()

        fun fromDto(dto: V3BulkIntlResponse): LocalizedTitles {
            return LocalizedTitles(
                movies = dto.movie.toTitles(),
                shows = dto.show.toTitles(),
                episodes = dto.episode.toTitles(),
            )
        }

        private fun Map<String, V3BulkIntlResponse.V3IntlTitle>.toTitles(): Map<TraktId, String> {
            return entries
                .mapNotNull { (id, value) ->
                    val traktId = id.toIntOrNull() ?: return@mapNotNull null
                    val title = value.title?.ifBlank { null } ?: return@mapNotNull null
                    TraktId(traktId) to title
                }
                .toMap()
        }
    }
}

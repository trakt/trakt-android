package tv.trakt.trakt.core.lists.usecases

import tv.trakt.trakt.common.core.translations.model.LocalizedTitles
import tv.trakt.trakt.common.core.translations.usecase.GetLocalizedTitlesUseCase
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.core.lists.model.CustomListItem
import tv.trakt.trakt.core.lists.model.SmartListItem

internal class LocalizeListItemsUseCase(
    private val getLocalizedTitlesUseCase: GetLocalizedTitlesUseCase,
) {
    suspend fun localize(items: List<CustomListItem>): List<CustomListItem> {
        val titles = getLocalizedTitlesUseCase.getTitles(
            movieIds = items.filterIsInstance<CustomListItem.MovieItem>().map { it.movie.ids.trakt },
            showIds = items.mapNotNull { it.show()?.ids?.trakt },
            episodeIds = items.filterIsInstance<CustomListItem.EpisodeItem>().map { it.episode.ids.trakt },
        )
        return items.map { it.localized(titles) }
    }

    suspend fun localizeSmart(items: List<SmartListItem>): List<SmartListItem> {
        val titles = getLocalizedTitlesUseCase.getTitles(
            movieIds = items.filterIsInstance<SmartListItem.MovieItem>().map { it.movie.ids.trakt },
            showIds = items.filterIsInstance<SmartListItem.ShowItem>().map { it.show.ids.trakt },
        )
        return items.map {
            when (it) {
                is SmartListItem.MovieItem -> it.copy(movie = titles.movie(it.movie))
                is SmartListItem.ShowItem -> it.copy(show = titles.show(it.show))
            }
        }
    }

    private fun CustomListItem.show(): Show? {
        return when (this) {
            is CustomListItem.MovieItem -> null
            is CustomListItem.ShowItem -> show
            is CustomListItem.SeasonItem -> show
            is CustomListItem.EpisodeItem -> show
        }
    }

    private fun CustomListItem.localized(titles: LocalizedTitles): CustomListItem {
        return when (this) {
            is CustomListItem.MovieItem -> copy(movie = titles.movie(movie))
            is CustomListItem.ShowItem -> copy(show = titles.show(show))
            is CustomListItem.SeasonItem -> copy(show = titles.show(show))
            is CustomListItem.EpisodeItem -> copy(
                show = titles.show(show),
                episode = titles.episode(episode),
            )
        }
    }
}

package tv.trakt.trakt.core.lists.usecases

import tv.trakt.trakt.common.core.translations.model.LocalizedTitles
import tv.trakt.trakt.common.core.translations.usecase.GetLocalizedTitlesUseCase
import tv.trakt.trakt.core.lists.model.CustomListItem

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

    private fun CustomListItem.show() =
        when (this) {
            is CustomListItem.MovieItem -> null
            is CustomListItem.ShowItem -> show
            is CustomListItem.SeasonItem -> show
            is CustomListItem.EpisodeItem -> show
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

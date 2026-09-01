package tv.trakt.trakt.core.home.sections.recommended.data.shows

import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.recommended.model.RecommendedItem

internal interface RecommendedShowsLocalDataSource {
    suspend fun setShows(shows: List<RecommendedItem.ShowItem>)

    suspend fun getShows(): List<RecommendedItem.ShowItem>

    suspend fun removeShow(id: TraktId)

    suspend fun clear()
}

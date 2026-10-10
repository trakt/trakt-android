package tv.trakt.trakt.core.home.sections.recommended.data.media

import tv.trakt.trakt.common.model.MediaType
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.recommended.model.RecommendedItem

internal interface RecommendedMediaLocalDataSource {
    suspend fun setItems(items: List<RecommendedItem>)

    suspend fun getItems(): List<RecommendedItem>

    suspend fun removeItem(
        id: TraktId,
        type: MediaType,
    )
}

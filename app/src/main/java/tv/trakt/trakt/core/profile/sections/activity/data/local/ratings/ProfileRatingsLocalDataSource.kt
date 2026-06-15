package tv.trakt.trakt.core.profile.sections.activity.data.local.ratings

import tv.trakt.trakt.core.profile.sections.activity.model.ProfileRatingItem

internal interface ProfileRatingsLocalDataSource {
    suspend fun addItems(items: List<ProfileRatingItem>)

    suspend fun setItems(items: List<ProfileRatingItem>)

    suspend fun getItems(): List<ProfileRatingItem>

    fun clear()
}

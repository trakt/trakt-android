package tv.trakt.trakt.core.profile.sections.activity.data.local.comments

import tv.trakt.trakt.core.profile.sections.activity.model.ProfileCommentItem

internal interface ProfileCommentsLocalDataSource {
    suspend fun addItems(items: List<ProfileCommentItem>)

    suspend fun setItems(items: List<ProfileCommentItem>)

    suspend fun getItems(): List<ProfileCommentItem>

    fun clear()
}

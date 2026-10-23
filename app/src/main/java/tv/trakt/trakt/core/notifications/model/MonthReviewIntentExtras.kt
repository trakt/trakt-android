package tv.trakt.trakt.core.notifications.model

import kotlinx.serialization.Serializable

@Serializable
internal data class MonthReviewIntentExtras(
    val year: Int,
    val month: Int,
    val userSlug: String,
)

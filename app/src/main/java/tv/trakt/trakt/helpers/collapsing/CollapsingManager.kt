package tv.trakt.trakt.helpers.collapsing

internal interface CollapsingManager {
    suspend fun isCollapsed(key: String): Boolean
}

package tv.trakt.trakt.helpers.collapsing

import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

/**
 * Manages the collapsed/expanded state of various sections in the app.
 */
internal interface CollapsingManager {
    /**
     * Returns true if the section identified by [key] is collapsed, false otherwise.
     */
    fun isCollapsed(key: CollapsingKey): Boolean

    /**
     * Marks the section identified by [key] as collapsed.
     */
    suspend fun collapse(key: CollapsingKey)

    /**
     * Marks the section identified by [key] as expanded.
     */
    suspend fun expand(key: CollapsingKey)

    /**
     * Clears all collapsing states.
     */
    suspend fun clear()
}

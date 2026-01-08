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
     * Returns true if the section identified by [keyId] is collapsed, false otherwise.
     */
    fun isCollapsed(keyId: String): Boolean

    /**
     * Marks the section identified by [key] as collapsed.
     */
    suspend fun collapse(key: CollapsingKey)

    /**
     * Marks the section identified by [keyId] as collapsed.
     */
    suspend fun collapse(keyId: String)

    /**
     * Marks the section identified by [key] as expanded.
     */
    suspend fun expand(key: CollapsingKey)

    /**
     * Marks the section identified by [keyId] as expanded.
     */
    suspend fun expand(keyId: String)

    /**
     * Clears all collapsing states.
     */
    suspend fun clear()
}

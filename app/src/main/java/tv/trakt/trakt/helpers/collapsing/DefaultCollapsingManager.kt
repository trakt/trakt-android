package tv.trakt.trakt.helpers.collapsing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

internal class DefaultCollapsingManager(
    private val dataStore: DataStore<Preferences>,
) : CollapsingManager {
    override suspend fun isCollapsed(key: String): Boolean {
        TODO("Not yet implemented")
    }
}

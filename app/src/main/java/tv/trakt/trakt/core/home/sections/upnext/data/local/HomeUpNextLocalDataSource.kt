package tv.trakt.trakt.core.home.sections.upnext.data.local

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.home.sections.upnext.model.ProgressShow
import java.time.Instant

internal interface HomeUpNextLocalDataSource {
    suspend fun addItems(
        items: List<ProgressShow>,
        ignoreUpdate: Boolean,
    )

    suspend fun getItems(): List<ProgressShow>

    suspend fun removeItems(
        showIds: List<TraktId>,
        ignoreUpdate: Boolean,
    )

    fun observeUpdates(): Flow<Instant?>

    fun clear()
}

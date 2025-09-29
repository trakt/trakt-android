package tv.trakt.trakt.core.lists.sections.personal.data.local

import kotlinx.coroutines.flow.Flow
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.TraktId
import java.time.Instant

internal interface ListsPersonalLocalDataSource {
    suspend fun setItems(items: List<CustomList>)

    suspend fun getItems(): List<CustomList>

    suspend fun editItem(
        listId: TraktId,
        name: String,
        description: String?,
        notify: Boolean,
    )

    suspend fun deleteItem(
        id: TraktId,
        notify: Boolean,
    )

    fun observeUpdates(): Flow<Instant?>

    fun clear()
}

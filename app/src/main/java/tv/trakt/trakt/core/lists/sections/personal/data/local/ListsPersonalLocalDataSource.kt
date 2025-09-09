package tv.trakt.trakt.core.lists.sections.personal.data.local

import tv.trakt.trakt.common.model.CustomList

internal interface ListsPersonalLocalDataSource {
    suspend fun addItems(items: List<CustomList>)

    suspend fun getItems(): List<CustomList>

    fun clear()
}

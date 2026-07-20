package tv.trakt.trakt.common.core.user.data.local.library

import tv.trakt.trakt.common.core.library.LibraryItem

interface UserLibraryLocalDataSource {
    suspend fun addItems(items: List<LibraryItem>)

    suspend fun setItems(items: List<LibraryItem>)

    suspend fun isLoaded(): Boolean

    suspend fun getAll(): List<LibraryItem>

    fun clear()
}

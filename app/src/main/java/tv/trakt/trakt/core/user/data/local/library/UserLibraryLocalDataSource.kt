package tv.trakt.trakt.core.user.data.local.library

import tv.trakt.trakt.core.library.model.LibraryItem

internal interface UserLibraryLocalDataSource {
    suspend fun addItems(items: List<LibraryItem>)

    suspend fun setItems(items: List<LibraryItem>)

    suspend fun isLoaded(): Boolean

    suspend fun getAll(): List<LibraryItem>

    fun clear()
}

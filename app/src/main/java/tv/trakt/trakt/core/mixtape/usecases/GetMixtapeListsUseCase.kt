package tv.trakt.trakt.core.mixtape.usecases

import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserListsUseCase
import tv.trakt.trakt.common.data.remote.ListsRemoteSource
import tv.trakt.trakt.common.model.lists.CustomList
import tv.trakt.trakt.core.mixtape.model.MixtapeTarget.PersonalList

internal class GetMixtapeListsUseCase(
    private val sessionManager: SessionManager,
    private val loadListsUseCase: LoadUserListsUseCase,
    private val listsRemoteSource: ListsRemoteSource,
    private val dataChanges: DataChanges,
) {
    suspend fun getLists(): List<PersonalList> {
        val userId = sessionManager.getProfile()?.ids?.trakt ?: return emptyList()

        return loadListsUseCase.loadLists().values
            .filter { it.ownerId == userId }
            .sortedBy { it.displayOrder }
            .map {
                PersonalList(
                    id = it.id,
                    ownerId = it.ownerId,
                    name = it.name,
                )
            }
    }

    suspend fun createList(name: String): PersonalList {
        val userId = checkNotNull(sessionManager.getProfile()) { "Creating a list needs a signed-in user" }.ids.trakt
        val id = listsRemoteSource.createList(
            name = name.trim(),
            description = null,
            privacy = CustomList.Privacy.Private.value,
        )
        dataChanges.notify(Kind.Lists)

        return PersonalList(
            id = id,
            ownerId = userId,
            name = name.trim(),
        )
    }
}

package tv.trakt.trakt.core.users.sections.lists.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.core.user.data.remote.otherlists.UserOtherListsRemoteDataSource
import tv.trakt.trakt.common.core.user.data.remote.personallists.UserPersonalListsRemoteDataSource
import tv.trakt.trakt.common.model.CustomList
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.pagination.Pagination

internal class GetUserProfileListsUseCase(
    private val personalListsSource: UserPersonalListsRemoteDataSource,
    private val otherListsSource: UserOtherListsRemoteDataSource,
) {
    suspend fun getPersonalLists(
        userId: TraktId,
        pagination: Pagination,
    ): ImmutableList<CustomList> {
        return personalListsSource
            .getPersonalLists(
                pagination = pagination,
                userId = userId.value.toString(),
            )
            .sortedByDescending { it.updatedAt }
            .map { CustomList.fromDto(it) }
            .toImmutableList()
    }

    suspend fun getCollaborationLists(
        userId: TraktId,
        pagination: Pagination,
    ): ImmutableList<CustomList> {
        return otherListsSource
            .getCollaborationLists(
                userId = userId.value.toString(),
            )
            .sortedByDescending { it.updatedAt }
            .take(pagination.limit)
            .map { CustomList.fromDto(it) }
            .toImmutableList()
    }
}

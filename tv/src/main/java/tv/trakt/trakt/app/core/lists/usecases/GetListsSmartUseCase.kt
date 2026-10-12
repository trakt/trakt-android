package tv.trakt.trakt.app.core.lists.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import timber.log.Timber
import tv.trakt.trakt.common.core.user.data.remote.smartlists.UserSmartListsRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Images
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.lists.SmartListSource
import tv.trakt.trakt.common.model.lists.SmartListSource.Unknown
import tv.trakt.trakt.common.model.pagination.Pagination

private const val PREVIEW_POSTERS_LIMIT = 8

private val supportedSources = SmartListSource.entries
    .filter { it != Unknown }
    .map { it.value }

internal class GetListsSmartUseCase(
    private val remoteSource: UserSmartListsRemoteDataSource,
) {
    suspend fun getLists(): ImmutableList<SmartList> {
        return remoteSource.getSmartLists()
            .filter { it.source in supportedSources }
            .asyncMap { SmartList.fromDto(it) }
            .asyncMap { it.withPreviewImages() }
            .toImmutableList()
    }

    private suspend fun SmartList.withPreviewImages(): SmartList {
        return try {
            copy(images = getPreviewImages(this))
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.w(error, "Error loading smart list %d posters", ids.trakt.value)
            }
            this
        }
    }

    private suspend fun getPreviewImages(list: SmartList): Images {
        val posters = remoteSource.getSmartListItems(
            listId = list.ids.trakt,
            pagination = Pagination(limit = PREVIEW_POSTERS_LIMIT),
            extended = "images",
        ).mapNotNull { item ->
            item.show?.images?.poster?.firstOrNull()
                ?: item.movie?.images?.poster?.firstOrNull()
        }

        return Images(posters = posters.toImmutableList())
    }
}

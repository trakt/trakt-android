package tv.trakt.trakt.core.lists.sections.smart.templates.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import timber.log.Timber
import tv.trakt.trakt.common.data.local.SmartListTemplatesLocalSource
import tv.trakt.trakt.common.data.remote.UserSmartListsRemoteSource
import tv.trakt.trakt.common.helpers.extensions.asyncMap
import tv.trakt.trakt.common.helpers.extensions.nowUtcInstant
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.lists.SmartList
import tv.trakt.trakt.common.model.lists.SmartListTemplate
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.lists.sections.smart.templates.model.SmartListTemplateCopy

private const val PERSONAL_PREVIEW_LIMIT = 3

internal class GetSmartListTemplatesUseCase(
    private val remoteSource: UserSmartListsRemoteSource,
    private val localSource: SmartListTemplatesLocalSource,
) {
    suspend fun getLocalTemplates(): ImmutableList<SmartListTemplate>? {
        return localSource.getTemplates(now = nowUtcInstant())
    }

    suspend fun getTemplates(): ImmutableList<SmartListTemplate> {
        val templates = remoteSource.getSmartListTemplates()
            .map(SmartListTemplate::fromDto)
            .filter { SmartListTemplateCopy.fromId(it.id) != null }
            .asyncMap { it.withPersonalPreview() }
            .toImmutableList()

        localSource.setTemplates(
            templates = templates,
            now = nowUtcInstant(),
        )
        return templates
    }

    suspend fun getUserSmartLists(): ImmutableList<SmartList> {
        return remoteSource.getSmartLists()
            .map(SmartList::fromDto)
            .toImmutableList()
    }

    private suspend fun SmartListTemplate.withPersonalPreview(): SmartListTemplate {
        if (!personal) {
            return this
        }

        return try {
            val items = remoteSource.getSmartListPreviewItems(
                filters = filters,
                pagination = Pagination(page = 1, limit = PERSONAL_PREVIEW_LIMIT),
                extended = "min",
            )
            copy(images = SmartListTemplate.imagesFromItems(items))
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
            this
        }
    }
}

package tv.trakt.trakt.core.lists.features.addfrom

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import tv.trakt.trakt.common.core.lists.usecases.ListItemLimitException
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.core.lists.features.addfrom.model.AddFromListsItem
import tv.trakt.trakt.core.lists.features.addfrom.model.AddFromListsPick
import tv.trakt.trakt.core.lists.features.addfrom.model.AddFromListsSource

@Immutable
internal data class AddFromListsState(
    val listName: String = "",
    val sources: ImmutableList<AddFromListsSource> = persistentListOf(),
    val activeSource: AddFromListsSource? = null,
    val items: ImmutableList<AddFromListsItem>? = null,
    val hasMoreItems: Boolean = false,
    val listedKeys: ImmutableSet<String>? = null,
    val picks: ImmutableList<AddFromListsPick> = persistentListOf(),
    val loading: LoadingState = LoadingState.Idle,
    val loadingMore: LoadingState = LoadingState.Idle,
    val adding: LoadingState = LoadingState.Idle,
    val error: Exception? = null,
    val ready: Boolean = false,
) {
    private val listed: Set<String> = listedKeys.orEmpty()

    val effectivePicks: ImmutableList<AddFromListsPick> = picks
        .filter { it.key !in listed }
        .toImmutableList()

    val pickedKeys: Set<String> = effectivePicks.map { it.key }.toSet()

    val pickedCounts: ImmutableMap<String, Int> = effectivePicks
        .groupingBy { it.sourceKey }
        .eachCount()
        .toImmutableMap()

    val candidates: ImmutableList<AddFromListsPick> = items.orEmpty()
        .filter { it.key !in listed }
        .mapNotNull { item ->
            activeSource?.let { AddFromListsPick.from(item, it.key) }
        }
        .toImmutableList()

    val isAllPicked: Boolean = candidates.isNotEmpty() &&
        candidates.all { it.key in pickedKeys }

    val alreadyInListCount: Int = items.orEmpty().count { it.key in listed }

    val isAddEnabled: Boolean = effectivePicks.isNotEmpty() &&
        !adding.isLoading &&
        listedKeys != null

    val hasChanges: Boolean = effectivePicks.isNotEmpty()

    fun isListed(item: AddFromListsItem): Boolean = item.key in listed
}

internal sealed interface AddFromListsResult {
    data class Added(
        val count: Int,
    ) : AddFromListsResult

    data class LimitReached(
        val error: ListItemLimitException,
    ) : AddFromListsResult

    data object Failed : AddFromListsResult
}

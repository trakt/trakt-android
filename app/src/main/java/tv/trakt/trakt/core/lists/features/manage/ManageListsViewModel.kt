package tv.trakt.trakt.core.lists.features.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.sync.changes.DataChange.Kind
import tv.trakt.trakt.common.core.sync.changes.DataChanges
import tv.trakt.trakt.common.core.user.usecases.lists.LoadUserListsUseCase
import tv.trakt.trakt.common.firebase.analytics.Analytics
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.errors.GlobalErrorsManager
import tv.trakt.trakt.common.helpers.extensions.HTTP_ERROR_TRAKT_VIP_LIMIT
import tv.trakt.trakt.common.helpers.extensions.getHttpCode
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.common.model.lists.CustomListMinimal
import tv.trakt.trakt.core.lists.features.manage.ManageListsTarget.EpisodeTarget
import tv.trakt.trakt.core.lists.features.manage.ManageListsTarget.SeasonTarget
import tv.trakt.trakt.core.lists.sections.personal.usecases.manage.AddPersonalListItemUseCase
import tv.trakt.trakt.core.lists.sections.personal.usecases.manage.RemovePersonalListItemUseCase

@Suppress("UNCHECKED_CAST")
internal class ManageListsViewModel(
    private val target: ManageListsTarget,
    private val sessionManager: SessionManager,
    private val loadListsUseCase: LoadUserListsUseCase,
    private val addListItemUseCase: AddPersonalListItemUseCase,
    private val removeListItemUseCase: RemovePersonalListItemUseCase,
    private val dataChanges: DataChanges,
    private val errorsManager: GlobalErrorsManager,
    private val analytics: Analytics,
) : ViewModel() {
    private val initialState = ManageListsState()

    private val listsState = MutableStateFlow(initialState.lists)
    private val collaborationsState = MutableStateFlow(initialState.collaborations)
    private val listedIdsState = MutableStateFlow(initialState.listedIds)
    private val togglingState = MutableStateFlow(initialState.toggling)
    private val userState = MutableStateFlow(initialState.user)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val user = sessionManager.getProfile()
                .also { user -> userState.update { user } }
                ?: return@launch

            try {
                loadingState.update { LoadingState.Loading }

                val (ownLists, collaborationLists) = loadListsUseCase.loadLocalLists()
                    .values
                    .partition { it.ownerId == user.ids.trakt }

                listsState.update { ownLists.toImmutableList() }
                collaborationsState.update { collaborationLists.toImmutableList() }
                listedIdsState.update { loadListedIds() }

                listsState.update(::sortedByListed)
                collaborationsState.update(::sortedByListed)

                loadingState.update { LoadingState.Done }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    errorState.update { error }
                    Timber.recordError(error)
                    loadingState.update { LoadingState.Idle }
                }
            }
        }
    }

    private suspend fun loadListedIds(): ImmutableSet<TraktId> {
        return when (target) {
            is SeasonTarget -> loadListsUseCase.loadSeasonLists(target.id)
            is EpisodeTarget -> loadListsUseCase.loadEpisodeLists(target.id)
        }
    }

    fun toggleList(list: CustomListMinimal) {
        if (loadingState.value.isLoading ||
            togglingState.value.contains(list.id) ||
            userState.value == null
        ) {
            return
        }

        val listed = isListed(list.id)
        togglingState.update { (it + list.id).toImmutableSet() }

        viewModelScope.launch {
            try {
                when {
                    listed -> removeItem(list)
                    else -> addItem(list)
                }
                setListed(list.id, listed = !listed)
                dataChanges.notify(Kind.Lists)
                if (listed) analytics.lists.logListItemRemove() else analytics.lists.logListItemAdd()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    when (error.getHttpCode()) {
                        HTTP_ERROR_TRAKT_VIP_LIMIT -> {
                            errorsManager.tryEmit(error)
                        }
                        else -> {
                            errorState.update { error }
                            Timber.recordError(error)
                        }
                    }
                }
            } finally {
                togglingState.update { (it - list.id).toImmutableSet() }
            }
        }
    }

    private suspend fun addItem(list: CustomListMinimal) {
        when (target) {
            is SeasonTarget -> addListItemUseCase.addSeason(
                listId = list.id,
                ownerId = list.ownerId,
                seasonId = target.id,
            )
            is EpisodeTarget -> addListItemUseCase.addEpisode(
                listId = list.id,
                ownerId = list.ownerId,
                episodeId = target.id,
            )
        }
    }

    private suspend fun removeItem(list: CustomListMinimal) {
        when (target) {
            is SeasonTarget -> removeListItemUseCase.removeSeason(
                listId = list.id,
                ownerId = list.ownerId,
                seasonId = target.id,
            )
            is EpisodeTarget -> removeListItemUseCase.removeEpisode(
                listId = list.id,
                ownerId = list.ownerId,
                episodeId = target.id,
            )
        }
    }

    private fun sortedByListed(lists: ImmutableList<CustomListMinimal>): ImmutableList<CustomListMinimal> {
        return lists
            .sortedBy { if (isListed(it.id)) 0 else 1 }
            .toImmutableList()
    }

    private fun isListed(listId: TraktId): Boolean {
        return listedIdsState.value.contains(listId)
    }

    private fun setListed(
        listId: TraktId,
        listed: Boolean,
    ) {
        listedIdsState.update {
            when {
                listed -> (it + listId).toImmutableSet()
                else -> (it - listId).toImmutableSet()
            }
        }
    }

    val state = combine(
        userState,
        listsState,
        collaborationsState,
        listedIdsState,
        togglingState,
        loadingState,
        errorState,
    ) { state ->
        ManageListsState(
            user = state[0] as User?,
            lists = state[1] as ImmutableList<CustomListMinimal>,
            collaborations = state[2] as ImmutableList<CustomListMinimal>,
            listedIds = state[3] as ImmutableSet<TraktId>,
            toggling = state[4] as ImmutableSet<TraktId>,
            loading = state[5] as LoadingState,
            error = state[6] as Exception?,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

package tv.trakt.trakt.core.userprofile.sections.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import tv.trakt.trakt.analytics.crashlytics.recordError
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.LoadingState.Done
import tv.trakt.trakt.common.helpers.LoadingState.Loading
import tv.trakt.trakt.common.helpers.extensions.EmptyImmutableList
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.profile.sections.social.model.SocialFilter
import tv.trakt.trakt.core.profile.sections.social.model.SocialFilter.Followers
import tv.trakt.trakt.core.profile.sections.social.model.SocialFilter.Following
import tv.trakt.trakt.core.profile.sections.social.model.SocialFilter.Requests
import tv.trakt.trakt.core.userprofile.sections.social.usecases.GetUserProfileSocialUseCase
import tv.trakt.trakt.helpers.collapsing.CollapsingManager
import tv.trakt.trakt.helpers.collapsing.model.CollapsingKey

internal class UserProfileSocialViewModel(
    private val userId: TraktId,
    private val getSocialUseCase: GetUserProfileSocialUseCase,
    private val collapsingManager: CollapsingManager,
) : ViewModel() {
    private val initialState = UserProfileSocialState()

    private val itemsState = MutableStateFlow(initialState.items)
    private val filterState = MutableStateFlow(initialState.filter)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val errorState = MutableStateFlow(initialState.error)
    private val collapseState = MutableStateFlow(collapsingManager.isCollapsed(CollapsingKey.USER_PROFILE_SOCIAL))

    private var loadDataJob: Job? = null
    private var collapseJob: Job? = null

    init {
        loadData()
    }

    fun loadData(ignoreErrors: Boolean = false) {
        loadDataJob?.cancel()
        loadDataJob = viewModelScope.launch {
            try {
                loadingState.update { Loading }
                itemsState.update {
                    when (filterState.value) {
                        Following -> getSocialUseCase.getFollowing(userId)
                        Followers -> getSocialUseCase.getFollowers(userId)
                        Requests -> EmptyImmutableList
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    if (!ignoreErrors) {
                        errorState.update { error }
                    }
                    Timber.recordError(error)
                }
            } finally {
                loadingState.update { Done }
                loadDataJob = null
            }
        }
    }

    fun setFilter(newFilter: SocialFilter) {
        if (newFilter == filterState.value || loadingState.value.isLoading) return
        filterState.update { newFilter }
        loadData()
    }

    fun setCollapsed(collapsed: Boolean) {
        collapseState.update { collapsed }
        collapseJob?.cancel()
        collapseJob = viewModelScope.launch {
            when {
                collapsed -> collapsingManager.collapse(CollapsingKey.USER_PROFILE_SOCIAL)
                else -> collapsingManager.expand(CollapsingKey.USER_PROFILE_SOCIAL)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    val state = combine(
        itemsState,
        filterState,
        loadingState,
        errorState,
        collapseState,
    ) { state ->
        UserProfileSocialState(
            items = state[0] as ImmutableList<User>?,
            filter = state[1] as SocialFilter,
            loading = state[2] as LoadingState,
            error = state[3] as Exception?,
            collapsed = state[4] as Boolean,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

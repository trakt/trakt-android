@file:Suppress("UNCHECKED_CAST")

package tv.trakt.trakt.core.userprofile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import timber.log.Timber
import tv.trakt.trakt.common.core.episodes.data.local.EpisodeLocalDataSource
import tv.trakt.trakt.common.core.movies.data.local.MovieLocalDataSource
import tv.trakt.trakt.common.core.shows.data.local.ShowLocalDataSource
import tv.trakt.trakt.common.core.user.usecases.blocking.BlockUserUseCase
import tv.trakt.trakt.common.core.user.usecases.blocking.GetBlockedUsersUseCase
import tv.trakt.trakt.common.core.user.usecases.following.FollowRequestUserUseCase
import tv.trakt.trakt.common.core.user.usecases.following.FollowUserUseCase
import tv.trakt.trakt.common.core.user.usecases.following.FollowUserUseCase.Result.Approved
import tv.trakt.trakt.common.core.user.usecases.following.FollowUserUseCase.Result.RequestPending
import tv.trakt.trakt.common.firebase.FirebaseConfig.RemoteKey.MOBILE_THIS_MONTH_IMAGE_URL
import tv.trakt.trakt.common.helpers.DynamicStringResource
import tv.trakt.trakt.common.helpers.LoadingState
import tv.trakt.trakt.common.helpers.StringResource
import tv.trakt.trakt.common.helpers.extensions.recordError
import tv.trakt.trakt.common.helpers.extensions.rethrowCancellation
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.Movie
import tv.trakt.trakt.common.model.Show
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.common.model.User
import tv.trakt.trakt.core.user.model.UserFollowRequest
import tv.trakt.trakt.core.user.usecases.social.LoadUserSocialRequestsUseCase
import tv.trakt.trakt.core.user.usecases.social.LoadUserSocialUseCase
import tv.trakt.trakt.core.user.usecases.social.updates.SocialUpdates
import tv.trakt.trakt.core.userprofile.UserProfileState.UserFollowRequestState
import tv.trakt.trakt.core.userprofile.navigation.UserProfileDestination
import tv.trakt.trakt.core.userprofile.sections.thismonth.GetUserProfileMonthUseCase
import tv.trakt.trakt.core.userprofile.usecases.GetUserProfileDetailsUseCase
import tv.trakt.trakt.resources.R

internal class UserProfileViewModel(
    savedStateHandle: SavedStateHandle,
    private val getUserProfileUseCase: GetUserProfileDetailsUseCase,
    private val getUserMonthUseCase: GetUserProfileMonthUseCase,
    private val getBlockedUsersUseCase: GetBlockedUsersUseCase,
    private val getSocialUseCase: LoadUserSocialUseCase,
    private val getSocialRequestsUseCase: LoadUserSocialRequestsUseCase,
    private val blockUserUseCase: BlockUserUseCase,
    private val followUserUseCase: FollowUserUseCase,
    private val followRequestUserUseCase: FollowRequestUserUseCase,
    private val showLocalDataSource: ShowLocalDataSource,
    private val episodeLocalDataSource: EpisodeLocalDataSource,
    private val movieLocalDataSource: MovieLocalDataSource,
    private val socialUpdates: SocialUpdates,
) : ViewModel() {
    private val destination = savedStateHandle.toRoute<UserProfileDestination>()
    private val user = Json.decodeFromString<User>(destination.userJson)

    private val initialState = UserProfileState(user = user)

    private val userState = MutableStateFlow(initialState.user)
    private val userBlockedState = MutableStateFlow(initialState.userBlocked)
    private val userFollowedState = MutableStateFlow(initialState.userFollowing)
    private val userRequestState = MutableStateFlow(initialState.userRequest)
    private val userMonthState = MutableStateFlow(initialState.monthStats)
    private val loadingState = MutableStateFlow(initialState.loading)
    private val infoState = MutableStateFlow(initialState.info)

    private val navigateShowState = MutableStateFlow(initialState.navigateShow)
    private val navigateMovieState = MutableStateFlow(initialState.navigateMovie)
    private val navigateEpisodeState = MutableStateFlow(initialState.navigateEpisode)

    private var processingJob: Job? = null

    init {
        loadData()
    }

    private fun loadData() {
        if (user.isPrivate) {
            with(viewModelScope) {
                launch {
                    loadFollowingStatus()
                    if (userFollowedState.value.following) {
                        loadMonthBackground()
                        launch { loadDetails() }
                        launch { loadMonthStats() }
                    }
                }
                launch { loadBlockedStatus() }
            }
            return
        }

        loadPublicData()
    }

    private fun loadPublicData() {
        with(viewModelScope) {
            loadMonthBackground()
            launch { loadDetails() }
            launch { loadMonthStats() }
            launch { loadFollowingStatus() }
            launch { loadRequestStatus() }
            launch { loadBlockedStatus() }
        }
    }

    private suspend fun loadFollowingStatus() {
        try {
            val social = getSocialUseCase.loadFollowingIds()
            userFollowedState.update {
                it.copy(
                    following = social.contains(user.ids.trakt),
                    loading = false,
                )
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
            userFollowedState.update {
                it.copy(loading = false)
            }
        }
    }

    private fun loadRequestStatus() {
        viewModelScope.launch {
            try {
                userRequestState.update {
                    UserFollowRequestState(
                        getSocialRequestsUseCase.loadRequests()
                            .find { it.user.ids.trakt == user.ids.trakt },
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            }
        }
    }

    private suspend fun loadDetails() {
        try {
            userState.update {
                getUserProfileUseCase.getUserProfileDetails(user.ids.trakt)
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        }
    }

    private suspend fun loadBlockedStatus() {
        try {
            val blockedUsers = getBlockedUsersUseCase.getBlockedUsers()
            val isBlocked = blockedUsers.any { it.key.ids.trakt == user.ids.trakt }

            userBlockedState.update {
                it.copy(
                    blocked = isBlocked,
                    loading = false,
                )
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
            userBlockedState.update {
                it.copy(loading = false)
            }
        }
    }

    private suspend fun loadMonthStats() {
        if (!user.isAnyVip) {
            // User is not VIP, skipping loading month stats as they are only available for VIP users.
            return
        }

        try {
            val stats = getUserMonthUseCase.getMonthStats(user.ids.trakt)
            userMonthState.update {
                UserProfileState.MonthlyStats(
                    stats = stats,
                    backgroundUrl = Firebase.remoteConfig.getString(MOBILE_THIS_MONTH_IMAGE_URL),
                    loading = false,
                )
            }
        } catch (error: Exception) {
            error.rethrowCancellation {
                Timber.recordError(error)
            }
        } finally {
            userMonthState.update {
                it?.copy(loading = false)
            }
        }
    }

    private fun loadMonthBackground() {
        if (!user.isAnyVip) {
            // User is not VIP, skipping loading month stats as they are only available for VIP users.
            return
        }

        userMonthState.update {
            UserProfileState.MonthlyStats(
                backgroundUrl = Firebase.remoteConfig.getString(MOBILE_THIS_MONTH_IMAGE_URL),
                stats = it?.stats,
                loading = it?.loading ?: true,
            )
        }
    }

    fun toggleUserFollowed() {
        viewModelScope.launch {
            try {
                val currentlyFollowing = userFollowedState.value.following

                userFollowedState.update {
                    it.copy(loading = true)
                }

                when (currentlyFollowing) {
                    true -> {
                        followUserUseCase.unfollowUser(user.ids.trakt)
                        userFollowedState.update { it.copy(following = false) }
                        infoState.update {
                            DynamicStringResource(
                                R.string.text_info_not_following,
                                listOf(user.displayName),
                            )
                        }
                    }
                    false -> {
                        val result = followUserUseCase.followUser(user.ids.trakt)
                        when (result) {
                            Approved -> {
                                userFollowedState.update { it.copy(following = true) }
                                infoState.update {
                                    DynamicStringResource(
                                        R.string.text_info_following,
                                        listOf(user.displayName),
                                    )
                                }
                            }
                            RequestPending -> {
                                userFollowedState.update { it.copy(following = false) }
                                infoState.update { DynamicStringResource(R.string.text_info_follow_request) }
                            }
                        }
                    }
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
            } finally {
                userFollowedState.update {
                    it.copy(loading = false)
                }
            }
        }
    }

    fun toggleUserBlocked() {
        viewModelScope.launch {
            try {
                val currentlyBlocked = userBlockedState.value.blocked

                userBlockedState.update {
                    it.copy(loading = true)
                }

                when (currentlyBlocked) {
                    true -> {
                        blockUserUseCase.unblockUser(user.ids.trakt)
                        infoState.update {
                            DynamicStringResource(
                                R.string.text_info_unblocked,
                                listOf(user.displayName),
                            )
                        }
                    }
                    false -> {
                        blockUserUseCase.blockUser(user.ids.trakt)
                        infoState.update {
                            DynamicStringResource(
                                R.string.text_info_blocked,
                                listOf(user.displayName),
                            )
                        }
                    }
                }

                userBlockedState.update {
                    it.copy(
                        blocked = !currentlyBlocked,
                        loading = false,
                    )
                }
            } catch (error: Exception) {
                error.rethrowCancellation {
                    Timber.recordError(error)
                }
                userBlockedState.update {
                    it.copy(loading = false)
                }
            }
        }
    }

    fun toggleFollowRequest(
        request: UserFollowRequest,
        approved: Boolean,
    ) {
        if (processingJob?.isActive == true) {
            return
        }
        processingJob = viewModelScope.launch {
            try {
                userRequestState.update { it.copy(loading = true) }

                if (approved) {
                    followRequestUserUseCase.approveRequest(request.id)
                    infoState.update {
                        DynamicStringResource(
                            R.string.text_info_follow_request_approved,
                            listOf(request.user.displayName),
                        )
                    }
                } else {
                    followRequestUserUseCase.rejectRequest(request.id)
                    infoState.update {
                        DynamicStringResource(
                            R.string.text_info_follow_request_rejected,
                            listOf(request.user.displayName),
                        )
                    }
                }

                userRequestState.update {
                    it.copy(
                        request = null,
                        loading = false,
                    )
                }
                socialUpdates.notifyUpdate()
            } catch (error: Exception) {
                error.rethrowCancellation {
                    userRequestState.update { it.copy(loading = false) }
                    Timber.recordError(error)
                }
            } finally {
                processingJob = null
            }
        }
    }

    fun navigateToShow(show: Show) {
        if (navigateShowState.value != null || processingJob?.isActive == true) {
            return
        }

        processingJob = viewModelScope.launch {
            showLocalDataSource.upsertShows(listOf(show))
            navigateShowState.update { show.ids.trakt }
        }
    }

    fun navigateToMovie(movie: Movie) {
        if (navigateMovieState.value != null || processingJob?.isActive == true) {
            return
        }

        processingJob = viewModelScope.launch {
            movieLocalDataSource.upsertMovies(listOf(movie))
            navigateMovieState.update { movie.ids.trakt }
        }
    }

    fun navigateToEpisode(
        show: Show,
        episode: Episode,
    ) {
        if (navigateEpisodeState.value != null || processingJob?.isActive == true) {
            return
        }

        processingJob = viewModelScope.launch {
            showLocalDataSource.upsertShows(listOf(show))
            episodeLocalDataSource.upsertEpisodes(listOf(episode))
            navigateEpisodeState.update { Pair(show.ids.trakt, episode) }
        }
    }

    fun clearNavigation() {
        navigateShowState.update { null }
        navigateMovieState.update { null }
        navigateEpisodeState.update { null }
    }

    fun clearInfo() {
        infoState.update { null }
    }

    val state = combine(
        userState,
        userBlockedState,
        userFollowedState,
        userRequestState,
        userMonthState,
        loadingState,
        navigateShowState,
        navigateMovieState,
        navigateEpisodeState,
        infoState,
    ) { states ->
        UserProfileState(
            user = states[0] as User,
            userBlocked = states[1] as UserProfileState.BlockedState,
            userFollowing = states[2] as UserProfileState.FollowingState,
            userRequest = states[3] as UserFollowRequestState,
            monthStats = states[4] as? UserProfileState.MonthlyStats,
            loading = states[5] as LoadingState,
            navigateShow = states[6] as? TraktId,
            navigateMovie = states[7] as? TraktId,
            navigateEpisode = states[8] as? Pair<TraktId, Episode>,
            info = states[9] as? StringResource,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = initialState,
    )
}

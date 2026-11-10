package tv.trakt.trakt.common.core.user.usecases.spoilers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.user.CollectionStateProvider
import tv.trakt.trakt.common.core.user.SpoilerState
import tv.trakt.trakt.common.core.user.usecases.progress.LoadUserProgressUseCase
import tv.trakt.trakt.common.data.local.ShowLocalSource
import tv.trakt.trakt.common.model.TraktId

@OptIn(ExperimentalCoroutinesApi::class)
class GetSpoilerStateUseCase(
    private val sessionManager: SessionManager,
    private val loadUserProgressUseCase: LoadUserProgressUseCase,
    private val showLocalSource: ShowLocalSource,
    private val collectionStateProvider: CollectionStateProvider,
) {
    fun observeEnabled(): Flow<Boolean> {
        return sessionManager.observeProfile()
            .map { it?.settings?.hideSpoilers == true }
            .distinctUntilChanged()
    }

    fun observe(): Flow<SpoilerState> {
        return combine(
            observeEnabled(),
            collectionStateProvider.stateFlow,
        ) { enabled, collection ->
            SpoilerState(
                enabled = enabled,
                collection = collection,
            )
        }
    }

    fun observeShowHidden(showId: TraktId): Flow<Boolean> {
        return observe().mapLatest { isShowHidden(showId) }
    }

    suspend fun isEnabled(): Boolean {
        return sessionManager.isAuthenticated() &&
            sessionManager.getProfile()?.settings?.hideSpoilers == true
    }

    suspend fun isShowHidden(showId: TraktId): Boolean {
        if (!isEnabled()) {
            return false
        }

        val airedEpisodes = showLocalSource.getShow(showId)?.airedEpisodes ?: 0
        if (airedEpisodes <= 0) {
            return true
        }

        val shows = when {
            loadUserProgressUseCase.isShowsLoaded() -> loadUserProgressUseCase.loadLocalShows()
            else -> loadUserProgressUseCase.loadShowsProgress()
        }
        val watchedEpisodes = shows
            .firstOrNull { it.showId == showId }
            ?.playsDistinctWithoutSpecials
            ?: 0

        return watchedEpisodes < airedEpisodes
    }
}

package tv.trakt.trakt.core.profile.sections.leaderboard.usecases

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import tv.trakt.trakt.common.auth.session.SessionManager
import tv.trakt.trakt.common.core.user.data.remote.UserRemoteDataSource
import tv.trakt.trakt.common.core.user.data.remote.social.UserSocialRemoteDataSource
import tv.trakt.trakt.common.model.UserStats
import tv.trakt.trakt.common.model.leaderboard.LeaderboardEntry
import tv.trakt.trakt.common.model.pagination.Pagination
import tv.trakt.trakt.core.profile.sections.leaderboard.data.local.LeaderboardLocalDataSource

internal class GetLeaderboardUseCase(
    private val socialRemoteSource: UserSocialRemoteDataSource,
    private val userRemoteSource: UserRemoteDataSource,
    private val localSource: LeaderboardLocalDataSource,
    private val sessionManager: SessionManager,
) {
    suspend fun getLocalLeaderboard(): ImmutableList<LeaderboardEntry>? {
        return localSource.getData()
    }

    suspend fun getLeaderboard(
        pagination: Pagination,
        saveLocal: Boolean = false,
    ): ImmutableList<LeaderboardEntry> {
        val entries = socialRemoteSource.getLeaderboard(pagination)
            .map(LeaderboardEntry::fromDto)
            .toImmutableList()

        if (saveLocal) {
            localSource.setData(entries)
        }

        return entries
    }

    suspend fun getViewerEntry(): LeaderboardEntry? {
        val user = sessionManager.getProfile() ?: return null
        if (!user.isAnyVip) return null

        val stats = userRemoteSource.getStats(userId = "me")
            ?.let(UserStats::fromDto)
            ?: return null

        return LeaderboardEntry(
            user = user,
            rank = null,
            totalMinutes = stats.totalMinutes,
            totalPlays = stats.totalPlays,
            isLocked = false,
            isViewer = true,
        )
    }
}

internal fun weaveLeaderboardViewer(
    entries: List<LeaderboardEntry>,
    viewer: LeaderboardEntry?,
): ImmutableList<LeaderboardEntry> {
    if (viewer == null) return entries.toImmutableList()

    val ownIndex = entries.indexOfFirst { it.user.ids.trakt == viewer.user.ids.trakt }
    if (ownIndex != -1) {
        return entries
            .mapIndexed { index, entry ->
                if (index == ownIndex) entry.copy(isViewer = true) else entry
            }
            .toImmutableList()
    }

    val viewerMinutes = viewer.totalMinutes ?: 0
    val insertAt = entries
        .indexOfFirst { it.isLocked || (it.totalMinutes ?: 0) < viewerMinutes }
        .takeIf { it != -1 }
        ?: entries.size

    val combined = entries.take(insertAt) + viewer + entries.drop(insertAt)

    var position = 0
    return combined
        .map { entry ->
            if (entry.isLocked) {
                entry
            } else {
                position += 1
                entry.copy(rank = position)
            }
        }
        .toImmutableList()
}

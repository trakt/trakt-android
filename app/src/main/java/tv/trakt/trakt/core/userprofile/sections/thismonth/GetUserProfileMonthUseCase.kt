package tv.trakt.trakt.core.userprofile.sections.thismonth

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.core.user.data.remote.history.UserHistoryRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.helpers.extensions.toInstant
import tv.trakt.trakt.common.helpers.extensions.toLocal
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.profile.sections.thismonth.model.ThisMonthStats
import java.time.ZoneOffset.UTC
import kotlin.time.toKotlinInstant

internal class GetUserProfileMonthUseCase(
    private val remoteUserSource: UserHistoryRemoteDataSource,
) {
    suspend fun getMonthStats(userId: TraktId): ThisMonthStats {
        return coroutineScope {
            val nowLocal = nowLocalDay()

            val fromDate = nowLocal.withDayOfMonth(1).atStartOfDay()
            val toDate = nowLocal.plusDays(1).atStartOfDay()

            val remoteEpisodesAsync = async {
                remoteUserSource.getEpisodesHistory(
                    userId = userId.value.toString(),
                    page = 1,
                    limit = 999,
                    from = fromDate.minusDays(2).toInstant(UTC).toKotlinInstant(),
                    to = toDate.plusDays(1).toInstant(UTC).toKotlinInstant(),
                    filters = null,
                )
                    .filter {
                        val watchedAt = it.watchedAt.toInstant().toLocal().toLocalDateTime()
                        watchedAt in fromDate..toDate
                    }
            }

            val remoteMoviesAsync = async {
                remoteUserSource.getMoviesHistory(
                    userId = userId.value.toString(),
                    page = 1,
                    limit = 999,
                    from = fromDate.minusDays(2).toInstant(UTC).toKotlinInstant(),
                    to = toDate.plusDays(1).toInstant(UTC).toKotlinInstant(),
                    filters = null,
                )
                    .filter {
                        val watchedAt = it.watchedAt.toInstant().toLocal().toLocalDateTime()
                        watchedAt in fromDate..toDate
                    }
            }

            val episodes = remoteEpisodesAsync.await()
            val movies = remoteMoviesAsync.await()

            ThisMonthStats(
                showsCount = episodes.distinctBy { it.show.ids.trakt }.size,
                episodesCount = episodes.size,
                moviesCount = movies.size,
            )
        }
    }
}

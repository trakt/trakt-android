package tv.trakt.trakt.core.userprofile.sections.thismonth

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import tv.trakt.trakt.common.core.user.data.remote.history.UserHistoryRemoteDataSource
import tv.trakt.trakt.common.helpers.extensions.nowLocalDay
import tv.trakt.trakt.common.model.TraktId
import tv.trakt.trakt.core.profile.sections.thismonth.model.ThisMonthStats
import java.time.ZoneOffset.UTC
import kotlin.time.toKotlinInstant

internal class GetUserProfileMonthUseCase(
    private val remoteUserSource: UserHistoryRemoteDataSource,
) {
    suspend fun getMonthStats(userId: TraktId): ThisMonthStats {
        return coroutineScope {
            val nowLocalDay = nowLocalDay()

            val fromDate = nowLocalDay.withDayOfMonth(1).atStartOfDay(UTC).toInstant().toKotlinInstant()
            val toDate = nowLocalDay.plusDays(1).atStartOfDay(UTC).toInstant().toKotlinInstant()

            val remoteEpisodesAsync = async {
                remoteUserSource.getEpisodesHistory(
                    userId = userId.value.toString(),
                    page = 1,
                    limit = 999,
                    from = fromDate,
                    to = toDate,
                    filters = null,
                )
            }

            val remoteMoviesAsync = async {
                remoteUserSource.getMoviesHistory(
                    userId = userId.value.toString(),
                    page = 1,
                    limit = 999,
                    from = fromDate,
                    to = toDate,
                    filters = null,
                )
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

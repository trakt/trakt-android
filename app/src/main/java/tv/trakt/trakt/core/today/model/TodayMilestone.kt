package tv.trakt.trakt.core.today.model

import androidx.compose.runtime.Immutable
import tv.trakt.trakt.common.model.Episode
import tv.trakt.trakt.common.model.EpisodeType

@Immutable
internal data class TodayMilestone(
    val type: Type,
    val season: Int,
) {
    enum class Type {
        SeasonStart,
        SeasonEnd,
        SeriesStart,
        SeriesEnd,
    }

    companion object {
        fun fromEpisode(episode: Episode): TodayMilestone? {
            val season = episode.season
            val isPilot = season == 1 && episode.number == 1

            val type = when (episode.type) {
                EpisodeType.SERIES_FINALE -> Type.SeriesEnd
                EpisodeType.SEASON_FINALE -> Type.SeasonEnd
                EpisodeType.SERIES_PREMIERE -> Type.SeriesStart
                EpisodeType.SEASON_PREMIERE -> if (isPilot) Type.SeriesStart else Type.SeasonStart
                EpisodeType.MID_SEASON_PREMIERE,
                EpisodeType.MID_SEASON_FINALE,
                null,
                -> if (isPilot) Type.SeriesStart else null
            } ?: return null

            return TodayMilestone(type = type, season = season)
        }

        fun strongest(milestones: Iterable<TodayMilestone?>): TodayMilestone? {
            return milestones
                .filterNotNull()
                .maxByOrNull { it.type.ordinal }
        }
    }
}

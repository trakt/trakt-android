package tv.trakt.trakt.core.ratings.rateprompt.usecases

import tv.trakt.trakt.common.model.EpisodeType
import tv.trakt.trakt.common.model.EpisodeType.MID_SEASON_FINALE
import tv.trakt.trakt.common.model.EpisodeType.SEASON_FINALE
import tv.trakt.trakt.common.model.EpisodeType.SERIES_FINALE

internal const val SHOW_BINGE_EPISODE_THRESHOLD = 3

/**
 * Decides whether a watched episode is a good moment to ask the user to rate its show.
 *
 * Deliberately does not reuse [EpisodeType.isFinale]: that property drives the calendar and
 * up-next tags and excludes [MID_SEASON_FINALE], which still counts as a rating moment here.
 */
internal class IsShowRatingCandidateUseCase {
    operator fun invoke(
        episodeType: EpisodeType?,
        showPlaysInBingeWindow: Int,
    ): Boolean {
        val isFinale = episodeType in finaleTypes
        val isBinge = showPlaysInBingeWindow >= SHOW_BINGE_EPISODE_THRESHOLD

        return isFinale || isBinge
    }

    private companion object {
        val finaleTypes = setOf(SERIES_FINALE, SEASON_FINALE, MID_SEASON_FINALE)
    }
}
